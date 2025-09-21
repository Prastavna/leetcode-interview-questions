<p align="center">
  <img src="src/main/web/public/leetcode-interview-questions.svg" alt="LeetCode Interview Questions" width="140" />
</p>

<h1 align="center">LeetCode Interview Questions</h1>

<p align="center">
  Automated pipeline for discovering, parsing, and visualising community-sourced interview experiences from LeetCode discussions.
</p>

<p align="center">
  <a href="https://img.shields.io/badge/java-21-4c8eda" target="_blank"><img src="https://img.shields.io/badge/java-21-4c8eda" alt="Java 21" /></a>
  <a href="https://img.shields.io/badge/build-Gradle%209.0-02303a" target="_blank"><img src="https://img.shields.io/badge/build-Gradle%209.0-02303a" alt="Gradle" /></a>
  <a href="https://img.shields.io/badge/ui-Vue%203-35495e" target="_blank"><img src="https://img.shields.io/badge/ui-Vue%203-35495e" alt="Vue" /></a>
  <a href="https://img.shields.io/badge/license-MIT-2c3e50" target="_blank"><img src="https://img.shields.io/badge/license-MIT-2c3e50" alt="License" /></a>
</p>

---

## Overview

This project powers a full ingestion-to-visualisation flow for public LeetCode discussion posts tagged as `interview`. The Java collector walks the GraphQL API, calls OpenAI to structure the content into a canonical `Interview` schema, validates and deduplicates the payloads, then persists them into a sorted JSON dataset. A Vite + Vue dashboard renders aggregated insights, charts, and searchable tables from that dataset.

Key capabilities:

- Reliable incremental backfill that walks LeetCode discussions until a configurable start date.
- Post-level filtering (e.g. discard posts with positive `UPVOTE` reactions) and validation to keep the dataset clean.
- Automated ID enrichment and date normalisation to simplify downstream analytics.
- Front-end visualisations built with Nuxt UI components, ECharts, and typed table sorting.

## Project Structure

```
├── build.gradle.kts             # Gradle build for the Java ingestion pipeline
├── src/main/java                # Fetcher, models, storage, and OpenAI integration
├── src/main/web                 # Vite + Vue 3 frontend for exploring the dataset
│   ├── public/interviews.json   # Published dataset consumed by the UI
│   └── public/leetcode-interview-questions.svg  # Brand assets
└── README.md                    # You are here
```

## Data Pipeline

1. **Fetch discussions** – `com.prastavna.leetcode.services.Leetcode` queries the LeetCode GraphQL API in pages, respecting rate limits and the configured `LEETCODE_FETCH_START_DATE`.
2. **Filter & dedupe** – Reactions with `UPVOTE` counts remove low-signal posts. Existing interviews are updated in-place using composite keys (`leetcodeId`, `id + date`).
3. **OpenAI parsing** – `Openai.getJsonCompletion` sends the discussion title + content to an OpenAI structured output model that fills the `Interview` POJO.
4. **Validation** – `InterviewValidator` enforces required fields (company, rounds with questions, YoE bounds). Invalid payloads are logged and skipped.
5. **Persistence** – `JsonStorage` appends or replaces entries, then sorts the array by `date` descending so the JSON data stays chronologically ordered.
6. **Visualisation** – The Vue dashboard consumes the generated dataset (drop it into `src/main/web/public/interviews.json`) to drive charts and tables.

> ℹ️ **LeetCode paging ceiling**: The GraphQL API currently stops returning results after `skip >= 3000`. When backfilling, watch the logs emitted in `App` to see when the crawler reaches that boundary.

## Getting Started

### Prerequisites

- Java 21+
- Gradle wrapper (bundled)
- OpenAI API key with Structured Output access
- Bun 1.1+ (recommended) or Node.js 18+ for the frontend

### 1. Configure Environment

Create a `.env` file based on the sample:

```bash
cp .env.sample .env
```

Fill in the variables:

| Variable | Description |
| --- | --- |
| `LEETCODE_FETCH_START_DATE` | Earliest date to retain (e.g. `2025-01-01`). |
| `LEETCODE_PAGE_SIZE` | Page size for GraphQL paging. 50 works well. |
| `LEETCODE_LAG_DAYS` | Skip extremely recent posts that may still change. |
| `OPENAI_BASE_URL` | Optional override for the OpenAI endpoint. |
| `OPENAI_API_KEY` | Required for parsing discussions into interviews. |
| `INTERVIEWS_JSON_PATH` | Output location for the dataset (`interviews.json` by default). |

### 2. Run the Ingestion Pipeline

```bash
./gradlew run
```

The CLI will page through LeetCode, print diagnostics for each stop/skip condition, invoke OpenAI, and write the resulting interviews into the configured JSON file. Logs such as `Breaking: created=...` or `Skipping topicId=...` help track paging progress.

### 3. Launch the Dashboard

```bash
cd src/main/web
bun install         # or npm install / pnpm install
bun run dev         # or npm run dev
```

Ensure `public/interviews.json` contains the dataset produced by the ingestion step (copy or symlink if needed). Then open the printed Vite URL to explore charts, filters, and tables.

### 4. Build Frontend for Production

```bash
cd src/main/web
bun run build       # or npm run build
```

Static assets will be emitted to `dist/` ready for deployment.

## Configuration & Customisation

- **Backfill window**: Adjust `LEETCODE_FETCH_START_DATE` to match your historical cut-off. The crawler will only stop once it reaches that date or the LeetCode paging ceiling.
- **Lag strategy**: `LEETCODE_LAG_DAYS` controls how many days to wait before ingesting extremely fresh posts.
- **Concurrency**: `OPENAI_CONCURRENCY` (optional `.env`) throttles the number of parallel OpenAI requests (defaults to min(cores, 4)).
- **Storage path**: Override `INTERVIEWS_JSON_PATH` if you want to version the dataset separately from the repo.

## Known Limitations

- The LeetCode GraphQL endpoint stops returning pages past `skip ≈ 3000`, so manual offsets are required beyond that.
- Some posts lack structured details (company, rounds). The validator intentionally discards low-signal data; relax the rules in `InterviewValidator` if you prefer partial records.
- OpenAI parsing quality depends on prompt adherence. Monitor skipped IDs and adjust the prompt or fallback handling as needed.

## Contributing

1. Fork & clone the repository.
2. Create a feature branch (`git checkout -b feature/my-change`).
3. Run `./gradlew run` and `bun run build` (or `npm run build`) to ensure everything still works.
4. Open a PR describing the motivation, approach, and screenshots/logs if relevant.

Issues and feature proposals are welcome—please include reproduction details or sample discussion links when reporting ingestion problems.

## Thanks

- LeetCode for providing open discussion data.
- OpenAI for structured completions that make parsing practical.
- Nuxt UI and ECharts for the frontend building blocks.

---

<p align="center">Built by <a href="https://prastavna.com">Prastavna</a> with ♥︎</p>

