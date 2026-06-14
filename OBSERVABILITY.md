# Observability - LinkTracker

Руководство по мониторингу: Prometheus, Grafana, метрики приложений и PromQL-запросы.

## Запуск мониторинг-стека

Мониторинг поднимается вместе с основным `docker compose`:

```bash
docker compose up --build
```

Компоненты в сети `link-tracker-net`:

| Компонент | Образ | Порт | Назначение |
| Prometheus | `prom/prometheus:v2.55.1` | 9090 | Сбор и хранение метрик |
| Grafana | `grafana/grafana-oss:11.5.2` | 3000 | Визуализация и алерты |
| Jaeger | `jaegertracing/all-in-one:1.57` | 16686 | Трейсинг (OpenTelemetry) |

### Проверка scrape-таргетов

1. Откройте http://localhost:9090/targets
2. Убедитесь, что jobs `scrapper` и `bot` в состоянии **UP**

Конфигурация Prometheus: `docker/prometheus/prometheus.yml`

```yaml
# scrapper - HTTP API и метрики на одном порту
targets: ["scrapper:8081"]
metrics_path: /metrics

# bot - приложение на 8080, метрики на отдельном порту
targets: ["bot:8011"]
metrics_path: /metrics
```

### Grafana

- URL: http://localhost:3000
- Логин / пароль по умолчанию: `admin` / `admin`
- Datasource Prometheus провиженится автоматически: `docker/grafana/provisioning/datasources/datasources.yml`
- Дашборды подгружаются из `docker/grafana/dashboards/`

| Дашборд | UID | Файл |
| Link Tracker — RED Metrics | `link-tracker-red` | `docker/grafana/dashboards/red-metrics.json` |
| Link Tracker — Business Metrics | `link-tracker-business` | `docker/grafana/dashboards/business-metrics.json` |

На обоих дашбордах есть переменная **Application** (`bot` / `scrapper`) в Dashboard Settings → Variables.

## Эндпоинты метрик

| Сервис | URL | Формат |
| Scrapper | http://localhost:8081/metrics | Prometheus text |
| Bot | http://localhost:8011/metrics | Prometheus text |

Метрики экспортируются через Spring Boot Actuator + Micrometer Prometheus Registry.

## Кастомные метрики

### Scrapper

| Метрика | Тип | Лейблы | Описание | Где пишется |
|---------|-----|--------|----------|-------------|
| `links_on_track_total` | Gauge | `tracked_source` (`github`, `stackoverflow`) | Количество ссылок на мониторинге | `LinksOnTrackMetrics` |
| `request_duration_ms` | Timer | `scope`, `scope_type` | Длительность операций (мс) | `OperationMetrics` |
| `api_requests_total` | Counter | `source` (`http_api`, `grpc`) | Запросы к API scrapper | `ApiMetricsFilter`, `GrpcApiMetricsInterceptor` |

**Значения `scope` для `request_duration_ms`:**

| scope | scope_type | Пример |
|-------|------------|--------|
| `database` | `JpaLinkRepositoryWrapper.getListOfLinksByChatId` | Время запроса к БД |
| `external_source` | `github`, `stackoverflow` | Длительность scrape |
| `kafka` | `link.raw-updates` | Публикация в Kafka (outbox) |

### Bot

| Метрика | Тип | Лейблы | Описание | Где пишется |
| `user_messages_total` | Counter | `request_type` (`command`, `text`) | Входящие сообщения Telegram | `TelegramUpdateHandler` |
| `command_requests_total` | Counter | `command` (`/track`, `/list`, …) | Обработанные команды | `CommandMetricsAspect` |
| `command_duration_ms` | Timer | `scope`, `scope_type` | Длительность обработки (мс) | `BotMetrics` |
| `sent_notification_total` | Counter | — | Отправленные уведомления | `TelegramBotService` |
| `linktracker_kafka_e2e_latency` | Timer | — | Kafka E2E: от header `lt-emitted-at` до обработки в боте | `LinkUpdatesKafkaListener` |

**Значения `scope` для `command_duration_ms`:**

| scope | scope_type | Когда |
| `telegram_command` | имя handler'а | Обработка команды пользователя |
| `scrapper_sync_api` | `getLinks`, `addLink`, … | Синхронный gRPC-вызов к scrapper |
| `scrapper_async_api` | `processLinkUpdate` | Обработка события из Kafka |

### RED-метрики (автоматические)

Spring Boot / Micrometer дополнительно экспортирует:

- `http_server_requests_seconds_*` — HTTP Rate / Errors / Duration
- `grpc_server_requests_seconds_*` — gRPC Rate / Duration
- `jvm_memory_used_bytes` — потребление памяти JVM

## Histogram и бакеты (buckets)

Кастомные Timer-метрики (`request_duration_ms`, `command_duration_ms`) используют Micrometer с `.publishPercentileHistogram()`.

**Почему не заданы вручную:** Micrometer автоматически подбирает SLA-бакеты под диапазон значений и совместим с `histogram_quantile()` в Prometheus. Это стандартный подход для Spring Boot 3+/4+ и избавляет от ручного подбора границ.

В `application.yaml` включены percentile histogram для ключевых метрик:

```yaml
# scrapper
management.metrics.distribution.percentiles-histogram:
  http.server.requests: true
  grpc.server.requests: true
  request_duration_ms: true

# bot
management.metrics.distribution.percentiles-histogram:
  http.server.requests: true
  command_duration_ms: true
  user_messages: true
  linktracker.kafka.e2e.latency: true
```

В Prometheus Timer экспортируется с суффиксом `_seconds` (значения в секундах, несмотря на `_ms` в имени метрики).

## PromQL-запросы

Полный список — в [example_pql.txt](example_pql.txt). Ниже — запросы с описанием панелей Grafana.

### RED-дашборд (`$application` = bot | scrapper)

| Панель | PromQL | Описание |
| Rate — HTTP | `sum(rate(http_server_requests_seconds_count{application="$application"}[1m]))` | HTTP-запросов в секунду |
| Rate — gRPC | `sum(rate(grpc_server_requests_seconds_count{application="$application"}[1m]))` | gRPC-запросов в секунду |
| Errors — HTTP 5xx | `sum(rate(http_server_requests_seconds_count{application="$application",status=~"5.."}[1m]))` | Ошибки 5xx в секунду |
| Duration — HTTP p95 | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application="$application"}[5m])) by (le))` | 95-й перцентиль HTTP-латентности |
| Duration — gRPC p95 | `histogram_quantile(0.95, sum(rate(grpc_server_requests_seconds_bucket{application="$application"}[5m])) by (le))` | 95-й перцентиль gRPC-латентности |
| Memory — JVM | `sum(jvm_memory_used_bytes{application="$application"}) by (area)` | Используемая память (heap / nonheap) |

### Бизнес-дашборд

| Панель | PromQL | Описание |
| Сообщения / с | `sum(rate(user_messages_total{application="bot"}[1m]))` | Входящие сообщения Telegram |
| По request_type | `sum(rate(user_messages_total{application="bot"}[1m])) by (request_type)` | Команды vs текст |
| Нотификации / с | `sum(rate(sent_notification_total{application="bot"}[1m]))` | Отправленные уведомления |
| Команды / с | `sum(rate(command_requests_total{application="bot"}[1m])) by (command)` | Обработанные команды |
| Перцентили команд | `histogram_quantile(0.95, sum(rate(command_duration_ms_seconds_bucket{application="bot", scope="telegram_command"}[5m])) by (le, scope_type))` | p50/p95/p99 обработки команд |
| Ссылки на мониторинге | `links_on_track_total{application="scrapper"}` | Активные ссылки по домену |
| API requests / с | `sum(rate(api_requests_total{application="scrapper"}[1m])) by (source)` | Запросы к scrapper API |
| Перцентили scrape | `histogram_quantile(0.95, sum(rate(request_duration_ms_seconds_bucket{application="scrapper", scope="external_source", scope_type=~"github\|stackoverflow"}[5m])) by (le, scope_type))` | p50/p95/p99 длительности опроса источников |

### Дополнительные запросы (не на дашборде, но полезны)

```promql
# p95 вызовов Scrapper gRPC из бота
histogram_quantile(0.95,
  sum(rate(command_duration_ms_seconds_bucket{application="bot", scope="scrapper_sync_api"}[5m])) by (le, scope_type))

# p95 операций с БД в scrapper
histogram_quantile(0.95,
  sum(rate(request_duration_ms_seconds_bucket{application="scrapper", scope="database"}[5m])) by (le, scope_type))

# p95 публикации в Kafka
histogram_quantile(0.95,
  sum(rate(request_duration_ms_seconds_bucket{application="scrapper", scope="kafka"}[5m])) by (le, scope_type))
```

## Алерты

В дашборде **RED Metrics** настроен алерт **High JVM heap usage**:

- **Условие:** `sum(jvm_memory_used_bytes{application="$application", area="heap"}) > 419430400` (400 MiB)
- **For:** 2 минуты
- **Сообщение:** `Heap-память приложения $application превысила 400 MiB`

Чтобы доставлять алерт в Telegram или Slack, настройте Contact Point в Grafana:  
**Alerting → Contact points → New contact point** и привяжите к правилу.

## Экспорт и импорт дашбордов

JSON-файлы дашбордов лежат в репозитории:

```
docker/grafana/dashboards/
├── red-metrics.json
└── business-metrics.json
```

Импорт вручную: Grafana → Dashboards → Import → Upload JSON.

При запуске через Docker Compose дашборды подгружаются автоматически (provisioning).

## Локальная разработка без Docker

1. Запустите scrapper и bot локально.
2. Обновите `docker/prometheus/prometheus.yml`, заменив `scrapper:8081` → `host.docker.internal:8081` (Windows/macOS) или IP хоста.
3. Перезапустите контейнер Prometheus: `docker compose up -d prometheus grafana`.

## Troubleshooting

| Проблема | Решение |

| Target DOWN в Prometheus | Проверьте, что контейнеры `scrapper` и `bot` запущены и в сети `link-tracker-net` |
| Пустые панели Grafana | Подождите 1–2 минуты после старта; проверьте переменную `$application` |
| Нет бизнес-метрик bot | Отправьте сообщение боту в Telegram — метрики появятся после активности |
| Нет scrape-метрик scrapper | Дождитесь цикла планировщика (интервал 40 с) или добавьте ссылку через `/track` |
