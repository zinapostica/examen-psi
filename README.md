# Expense Copilot

Receipt & expense copilot for the final exam stack: **Next.js (TypeScript)** UI + **Spring Boot** API + **Postgres** + image storage (local / Cloudinary-ready / GCS-optional).

**Flow:** upload receipt → vision extract (mock by default) → confirm form → list/filter expenses.

Curriculum alignment: Track A (vision / structured JSON, production hygiene) + Track B (Spring layered architecture, cloud deploy notes). Plan: project docs `receipt-expense-copilot-plan.md`.

---

## Architecture

```
Next.js (UI)  --REST/multipart-->  Spring Boot (layered)
                                      │
                                      ├── Postgres (expenses + receipt metadata)
                                      ├── Object storage (local | Cloudinary | GCS stub)
                                      └── ReceiptExtractor port (mock | OpenAI vision)
```

**Layering (Track B):** `web` controllers → `service` → `repository` / `storage` / `extract`.  
`ReceiptExtractor` is a hexagonal port so providers swap via `EXTRACTOR=mock|openai` without touching controllers.

---

## Quick start (local)

### Prerequisites

- Java 21+, Maven 3.9+
- Node 20+
- Postgres 16 (Docker Compose **or** local install)

### 1. Database

**Docker Compose (API + DB + web):**

```bash
docker compose up --build
```

- Web: http://localhost:3000  
- API: http://localhost:8080  
- Health: http://localhost:8080/actuator/health  

**Or Postgres only / native API:**

```bash
# example local roles (already used by defaults)
createdb expense_copilot   # or use docker: only the db service
```

```bash
cd backend
mvn spring-boot:run
```

```bash
cd frontend
cp .env.example .env.local   # NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
npm install
npm run dev
```

Defaults: `EXTRACTOR=mock`, `STORAGE=local`, uploads under `./uploads`.

### 2. Smoke check

```bash
curl -s http://localhost:8080/actuator/health
curl -s 'http://localhost:8080/api/expenses?month=2026-11'
```

---

## Demo script (≈5 min)

| Time | Show | Say |
|------|------|-----|
| 0:00 | Landing | One-liner + stack (Next, Spring, Postgres, mock/live vision). |
| 0:30 | Upload | Drop a receipt (or camera). Show compress → upload. |
| 1:15 | Extract | Fields fill from structured JSON; open Network / “Raw extract JSON”. |
| 2:30 | Confirm | Change category, **Confirm & save**. |
| 3:30 | Expenses | Filter month + category; open detail with thumbnail. |
| 4:30 | Deploy | Live URL or README deploy section; secrets + mock fallback. |

**Backup if AI keys missing:** keep `EXTRACTOR=mock` — fixture JSON still drives confirm → persist → list. Seed expenses load on first boot.

---

## API

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/receipts` | Multipart `file` upload |
| `POST` | `/api/receipts/{id}/extract` | Vision / mock → draft fields |
| `GET` | `/api/receipts/{id}` | Status + draft + image URL |
| `POST` | `/api/expenses` | Confirm + create expense |
| `GET` | `/api/expenses?month=YYYY-MM&category=FOOD` | List / filter |
| `GET` | `/api/expenses/{id}` | Detail |
| `GET` | `/actuator/health` | Health |

Validation: image type/size (≤5 MB), amount &gt; 0, required merchant/date/currency/category whitelist.

---

## Configuration

See [`.env.example`](./.env.example).

| Variable | Default | Notes |
|----------|---------|-------|
| `DATABASE_URL` | local Postgres JDBC | Neon/Supabase-ready (`?sslmode=require`) |
| `STORAGE` | `local` | `cloudinary` when keys set; `gcs` scaffolded |
| `EXTRACTOR` | `mock` | `openai` needs `OPENAI_API_KEY` |
| `CORS_ORIGINS` | `http://localhost:3000` | Comma-separated frontend origins |
| `NEXT_PUBLIC_API_BASE_URL` | `http://localhost:8080` | Baked into Next client |

**Secrets the user must add for full cloud demo (never commit):**

1. **Postgres** — Neon or Supabase connection string (`DATABASE_URL`, user, password).
2. **Optional vision** — `OPENAI_API_KEY` (+ set `EXTRACTOR=openai`).
3. **Optional images** — Cloudinary `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` with `STORAGE=cloudinary`.
4. **Optional GCS** — bucket + ADC/service account (exam Cloud Run path; not required for free-tier MVP).

Mock extract works with **zero** AI keys.

---

## Deploy (free / hobby path)

Preferred without paid GCP:

| Piece | Suggested | Notes |
|-------|-----------|-------|
| Frontend | **Vercel** | Root `frontend/`; set `NEXT_PUBLIC_API_BASE_URL` to API URL |
| Backend | **Railway / Render / Fly** | Deploy `backend/` Docker image; set DB + CORS |
| DB | **Neon** (or Supabase) | Free Postgres; JDBC URL in `DATABASE_URL` |
| Images | **Cloudinary** free tier or keep `STORAGE=local` ephemeral | Local disk on PaaS is wiped on restart |

### Vercel (frontend)

1. Import repo; set root directory to `frontend`.
2. Env: `NEXT_PUBLIC_API_BASE_URL=https://<your-api-host>`.
3. Redeploy after API URL is known.

### Railway / Render (API)

1. New service from `backend/Dockerfile`.
2. Attach Neon JDBC URL + `CORS_ORIGINS=https://<vercel-app>.vercel.app`.
3. `EXTRACTOR=mock` for keyless demo; `STORAGE=cloudinary` if you need durable images.

### GCP Cloud Run (exam Track B)

```bash
cd backend
# build & push image, then:
# gcloud run deploy expense-copilot-api --image ... --set-env-vars ...
```

Wire Cloud SQL or Neon, Secret Manager for keys, and optionally `STORAGE=gcs` after completing the GCS client (stubbed in code with clear error until wired).

### What blocked fully automatic deploy in this agent environment

- No Docker daemon on the cloud VM (compose verified as files only).
- No Vercel / Railway / Neon / Cloudinary account credentials for this agent.
- Cursor `publish-app` is for single Node web apps — this monorepo needs a separate Java API host.

Ship works locally + via `docker compose` where Docker is available; add the accounts/secrets above to go live.

---

## Cost notes

- Default **mock** extractor: $0.
- Live vision: prefer **gpt-4o-mini** (or Gemini Flash / DeepSeek vision). Client compresses to ~1600px JPEG @ 0.8 before upload.
- Rough ballpark: a few thousand image tokens per receipt → often **&lt; $0.01–0.03 / extract** on mini-class models (verify current pricing).
- Cloud Run scale-to-zero; avoid leaving Cloud SQL running for idle demos — Neon free tier is friendlier for homework.
- Set billing alerts on any AI provider; never put keys in the Next.js bundle.

---

## Security notes

- Content-type + magic-byte checks; 5 MB cap.
- Bean Validation on confirm; category enum whitelist.
- API keys only in server env.
- CORS limited to configured origins.
- Extract actions logged (uploadId, latency, provider) — not raw image bytes.
- Use sample/own receipts only in demos.

---

## Project layout

```
backend/     Spring Boot 3.4 API
frontend/    Next.js App Router UI
fixtures/    Shared mock extract JSON
uploads/     Local receipt blobs (gitignored)
docker-compose.yml
```
