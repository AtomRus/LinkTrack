# LinkTracker

LinkTracker - Telegram-бот, который отслеживает изменения на GitHub-репозиториях и вопросах Stack Overflow и оперативно информирует пользователя о них.

## Архитектура
```mermaid
graph TD

    classDef user fill:#248bc2,stroke:#1a658f,stroke-width:2px,color:#fff;
    classDef service fill:#2d3748,stroke:#1a202c,stroke-width:2px,color:#fff;
    classDef db fill:#2f855a,stroke:#22543d,stroke-width:2px,color:#fff;
    classDef broker fill:#b7791f,stroke:#744210,stroke-width:2px,color:#fff;
    classDef ext fill:#4a5568,stroke:#2d3748,stroke-width:1px,color:#cbd5e0;

    TG[Telegram User]:::user
    
    BOT[" bot <br> (8080 / metrics 8011)"]:::service
    SCRAP[" scrapper <br> (8081 / gRPC 9091)"]:::service
    AI[" ai-agent <br> (8082, опционально)"]:::service
    
    DB_PG[(PostgreSQL)]:::db
    DB_VK[(Valkey)]:::db
    
    K_RAW{{" Kafka <br> (link.raw-updates)"}}:::broker
    K_PROC{{" Kafka <br> (link.processed-updates)"}}:::broker
    
    API_GH[GitHub API]:::ext
    API_SO[Stack Overflow API]:::ext

    %% Связи и потоки данных
    TG -->|Взаимодействие| BOT
    BOT -->|gRPC| SCRAP
    
    SCRAP --> DB_PG
    SCRAP --> DB_VK
    SCRAP --> API_GH
    SCRAP --> API_SO
    
    SCRAP -->|Publish| K_RAW
    K_RAW -->|Consume| AI
    
    AI -->|Publish| K_PROC
    K_PROC -->|Consume| BOT
    
    BOT -->|Уведомление| TG

    %% Аннотация для обходного пути, если ai-agent выключен
    K_RAW -.->|Если AI отключен| BOT
```


 `scrapper`  Хранение ссылок, polling внешних API, отправка уведомлений 
 `bot`  Telegram UI, gRPC-клиент к scrapper, consumer Kafka 
 `ai-agent`  Фильтрация, суммаризация и группировка событий из Kafka 
 `load-service`  Нагрузочное тестирование (профиль `performance`) 
 `common-proto`  gRPC-контракты и Avro-схемы 

Подробнее о мониторинге: [OBSERVABILITY.md](OBSERVABILITY.md).

## Требования

- Maven 3.9.12+
- Docker и Docker Compose
- [Telegram Bot Token](https://t.me/botfather)
- GitHub Personal Access Token
- Stack Overflow API key

## Быстрый старт (Docker)

### 1. Секреты

Создайте файл `.env` в корне репозитория (он в `.gitignore`):

```env
TELEGRAM_TOKEN=your-telegram-bot-token
GITHUB_TOKEN=your-github-token
STACKOVERFLOW_KEY=your-stackoverflow-key
```

### 2. Запуск

```bash
docker compose up --build
```

После старта доступны:

 Сервис URL 
 Bot (HTTP)| http://localhost:8080 
 Bot metrics  http://localhost:8011/metrics 
 Scrapper (HTTP)  http://localhost:8081 
 Scrapper metrics  http://localhost:8081/metrics 
 Prometheus  http://localhost:9090 
 Grafana  http://localhost:3000 (admin / admin) 
 Jaeger UI  http://localhost:16686 

### 3. Остановка

```bash
# с удалением volumes
docker compose down -v

# повторный запуск (без пересборки)
docker compose up -d
```

## Локальная разработка (без Docker)

1. Поднимите инфраструктуру: PostgreSQL, Valkey, Kafka (или `docker compose up postgres valkey1 valkey2 valkey3 kafka1 kafka2 kafka3 kafka-init schema-registry`).
2. Экспортируйте переменные окружения (`TELEGRAM_TOKEN`, `GITHUB_TOKEN`, `STACKOVERFLOW_KEY`).
3. Соберите и запустите модули:

```bash
mvn clean package -DskipTests

# scrapper
java -jar scrapper/target/scrapper-*.jar

# bot
java -jar bot/target/bot-*.jar

# ai-agent (для полного Kafka-pipeline)
java -jar ai-agent/target/ai-agent-*.jar
```

## Сборка и тесты

```bash
# все модули
mvn clean verify

mvn clean verify -pl build-report-aggregate -am
```


## Команды бота

 Команда  Описание 
 `/start`  Главное меню 
 `/help`  Справка 
 `/track`  Добавить ссылку на отслеживание 
 `/untrack`  Убрать ссылку 
 `/list`  Список отслеживаемых ссылок 
 `/addTag`  Добавить тег к ссылке 
 `/removeTag`  Удалить тег 

Поддерживаемые источники: `github.com`, `stackoverflow.com`.

## Профиль performance

Нагрузочное тестирование и сравнение режимов планировщика (`VIRTUAL_THREADS` / `OS_THREADS` / `SINGLE_THREAD`):

```bash
docker compose --profile performance up --build
```

Сервис `load-service` доступен на порту `8090`.

## Полезные ссылки

- [OBSERVABILITY.md](OBSERVABILITY.md) — Prometheus, Grafana, метрики, PromQL
- [example_pql.txt](example_pql.txt) — PromQL-запросы для дашбордов
