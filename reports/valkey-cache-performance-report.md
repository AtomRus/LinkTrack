# Отчет по производительности Valkey-кэша

## Цель
Оценить, насколько кэширование в Valkey улучшает задержку чтения при получении списка ссылок в Scrapper.

## Тестовый стенд
- Среда: профиль производительности Docker Compose (`scrapper-perf`, `load-service`, `postgres-perf`).
- Сценарий чтения: повторные gRPC-вызовы `getLinks(chatId)` через endpoint `load-service` `POST /api/load/list-reads`.
- Подготовка данных: предварительное наполнение отслеживаемых ссылок для одного чата через `POST /api/load/seed-scrapper`.
- Прогрев: выполняется перед замерами для стабилизации JIT и пулов соединений.

## Реализованный endpoint нагрузки
- Новый endpoint: `POST /api/load/list-reads`
- Тело запроса:
  - `chatId`: идентификатор чата
  - `iterations`: количество запросов на чтение
  - `concurrency`: количество параллельных воркеров
  - `warmupIterations`: количество запросов прогрева
- Ответ: стандартный `LoadSummaryResponse` с длительностью, успехами/ошибками и расчетным RPS.

## Как запустить
1. Запустить perf-стек:
   - `docker compose --profile performance up --build -d`
2. Подготовить данные:
   - `curl -X POST http://localhost:8090/api/load/seed-scrapper -H "Content-Type: application/json" -d "{\"chatId\":1001,\"count\":5000,\"concurrency\":32,\"linkPrefix\":\"https://perf.example/link\"}"`
3. Прогнать с включенным кэшем:
   - `curl -X POST http://localhost:8090/api/load/list-reads -H "Content-Type: application/json" -d "{\"chatId\":1001,\"iterations\":20000,\"concurrency\":64,\"warmupIterations\":500}"`
4. Прогнать с выключенным кэшем (перезапустить `scrapper-perf` с `SPRING_CACHE_TYPE=none`):
   - `docker compose stop scrapper-perf`
   - `docker compose rm -f scrapper-perf`
   - Запустить `scrapper-perf` с `SPRING_CACHE_TYPE=none` и повторить шаг 3.

## Результаты
Используйте команды выше, чтобы получить два образца `LoadSummaryResponse` (cache ON и cache OFF), затем сравните:
- `durationMs`
- `rps`
- количество ошибок (`failed`)


## Ожидаемый результат
- При cache ON средняя задержка запроса должна снизиться, а RPS для повторных чтений одного `chatId` — вырасти.
- При cache OFF пропускная способность должна быть ниже из-за повторных чтений из репозитория/базы данных.

