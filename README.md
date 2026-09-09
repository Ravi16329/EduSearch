# EduSearch — Multi-Source Best Educational Content Finder

Finds the best YouTube lesson and top web articles for any topic, in the
language you pick. Videos are ranked by a blend of (log-scaled) view count
and recency; articles are left in the web search provider's native relevance
order, since ordinary webpages don't expose view counts — the UI says so
explicitly.

## Stack
- **Backend:** Java 17, Spring Boot 3.3 (Web, WebFlux client, Cache), Maven
- **Frontend:** React 18 + Vite, Axios
- No database: nothing is persisted in this pass (no accounts/history yet),
  so there's no datasource to configure.

## Project layout
```
backend/    Spring Boot API (GET /api/search)
frontend/   React app (Vite dev server, proxies /api to the backend)
```

## 1. Get API credentials
You need exactly two keys:

1. **YouTube Data API v3** — enable it in Google Cloud Console, create an API key.
2. **Tavily** — sign up at https://tavily.com and grab your API key from the
   dashboard. Tavily is used for the general web/article search (GFG,
   W3Schools, blogs, etc). Note: Tavily doesn't have a hard "restrict to
   language X" filter the way Google CSE's `lr` param does, so the selected
   language is folded into the query text as a soft hint instead (see
   `WebArticleService`).

## 2. Configure secrets (never hardcoded)
```bash
cd backend
cp .env.example .env
# edit .env with your real keys, then export them, e.g.:
export $(grep -v '^#' .env | xargs)
```

`.env.example` only has two lines:
```
YOUTUBE_API_KEY=your_youtube_data_api_v3_key
TAVILY_API_KEY=your_tavily_api_key
```

## 3. Run the backend
```bash
cd backend
mvn spring-boot:run
```
Starts on `http://localhost:8080`. No database to stand up — the search
endpoint is stateless and has no persistence dependency.

## 4. Run the frontend
```bash
cd frontend
npm install
npm run dev
```
Opens on `http://localhost:5173` and proxies `/api/*` to the backend.

## API

`GET /api/search?topic=Laplace%20Transform&subject=Math&lang=en`

```json
{
  "topic": "Laplace Transform",
  "subject": "Math",
  "lang": "en",
  "videos": [ { "videoId": "...", "title": "...", "channel": "...", "thumbnailUrl": "...", "viewCount": 128000, "publishedAt": "2023-05-01T00:00:00Z", "videoUrl": "...", "score": 0.81 } ],
  "articles": [ { "title": "...", "source": "geeksforgeeks.org", "snippet": "...", "url": "...", "rank": 1 } ],
  "videoRankingNote": "Videos are ranked by a blend of view count (log-scaled) and how recent the upload is.",
  "articleRankingNote": "These are top articles by relevance, not \"most viewed\" - regular webpages don't expose view counts.",
  "fromCache": false
}
```

Errors come back as clean JSON from the global exception handler, e.g.:
```json
{ "error": "API_QUOTA_EXCEEDED", "message": "We've hit the daily search limit for youtube. Please try again later.", "source": "youtube", "timestamp": "..." }
```

## Caching
Results are cached in-memory (`ConcurrentHashMap`, TTL-based) keyed by
`subject+topic+lang`, so repeat searches don't burn YouTube/Tavily quota.
TTL is configurable via `edusearch.cache.ttl-seconds` (default 30 min).

## Not built yet (by design)
User accounts, saved search history, and content translation (only language
*filtering/hinting*, not translation) are intentionally out of scope for this
pass — which is also why there's no database wired in right now.
