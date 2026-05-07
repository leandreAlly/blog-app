# Performance test suite

Two artefacts:

| File | Tool | Purpose |
|---|---|---|
| `postman_collection.json` | Postman | Functional smoke + manual latency feel for the hot endpoints |
| `blog-load-test.jmx` | JMeter 5.x | Concurrent load: 50 virtual users, 10s ramp-up, 10 iterations |

## Endpoints exercised

The plan focuses on the endpoints touched by this optimization phase:

- `GET /api/posts` — paginated list (cache + index path)
- `GET /api/posts/{id}` — drives `ViewCountService.record` (ConcurrentHashMap + LongAdder)
- `GET /api/posts/trending` — served from `TrendingIndex` snapshot
- `GET /api/posts/author/{authorId}/stats` — async, runs on `analyticsExecutor`
- `POST /api/posts/{id}/publish` — fires `PostPublishedEvent` on the notification pool

## Running JMeter from CLI

```bash
# Headless run, results to results/ folder
jmeter -n -t docs/perf/blog-load-test.jmx \
       -Jhost=localhost -Jport=8080 -Jjwt="$JWT" \
       -l docs/perf/results/run-$(date +%s).jtl \
       -e -o docs/perf/results/report-$(date +%s)
```

`$JWT` is a token from `POST /api/auth/login`. Open the generated `report-*/index.html` for the dashboard.

## Metrics to capture

Run it once before any optimization commit (baseline) and once after the
full chain is in place. Compare:

| Metric | Where to read it | What changed |
|---|---|---|
| p95 / p99 latency on `GET /api/posts/trending` | JMeter dashboard, `Response Times Over Time` | Should drop sharply once `TrendingIndex` is warm — served in-memory |
| Throughput on `GET /api/posts/author/{id}/stats` | JMeter `Statistics` table, throughput column | Should rise: async releases the Tomcat worker for other requests |
| Tomcat busy threads (`tomcat.threads.busy`) | `/actuator/metrics/tomcat.threads.busy` | Stays lower under the same load — work moved to dedicated executors |
| Cache hit ratio | `/actuator/metrics/cache.gets?tag=result:hit` | Should grow as cached endpoints are repeated |
| `analytics-*` thread pool active count | `/actuator/metrics/executor.active?tag=name:analyticsExecutor` | Confirms work is running on the right pool |
| Posts UPDATE rate | DB-side or app log (view-count flush) | Should be ~1 per minute per active post, not 1 per request |

## Postman quick-check

Import `postman_collection.json`, set the collection variable `baseUrl`
to your server, and run the requests sequentially. The `Tests` tab on
each request asserts a 200 and prints the response time so you can eyeball
end-to-end latency without running a full JMeter pass.
