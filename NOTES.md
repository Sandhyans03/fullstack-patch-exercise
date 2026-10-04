# NOTES

## Summary of changes
- **Search query (highest value):** `AND`/`OR` precedence was wrong, so archived tasks leaked into results and the status filter only applied to title matches. Fixed in the repository query, `db/queries/search_tasks.sql` and the Oracle package.
- **Backend:** removed an artificial `Thread.sleep` (shorter query = slower response, up to 1s); invalid `status`/`page`/`pageSize` now return 400 instead of 500; overflow-safe paging; `%`/`_` in searches matched literally; `id` tie-breaker for stable ordering.
- **Frontend:** a failed request left the UI stuck on "Loading..." (error never shown); slow old responses could overwrite newer results (now aborted); page wasn't reset on a new search/filter; added a 300ms search debounce.

## Assumptions
Archived tasks should never be listed. Max `pageSize` of 100. Invalid input is a client error (400).

## Not changed, and why
- Pagination is still in memory (fetch all, then slice). Fine for ~50 rows; moving it into SQL is the next step but a larger change to a query I couldn't run in the AI sandbox.
- `Task.status` is a `String` while `TaskStatus` is an enum; left to keep the diff small.
- No tests added to the repo.

## Biggest remaining risk
In-memory pagination loads the full result set per request, and there are no automated tests, so regressions in search would go unnoticed. The Oracle changes were not executed.

## Tools / AI used
Used Claude to review the code, draft the fixes, and write throwaway tests (deleted after use). Its sandbox couldn't download Maven, so the Spring Boot app was not run there: I compile-checked the Java against stubs and replayed the query logic on the seed data in SQLite. Frontend behaviour was checked with mocked-fetch tests that fail on the original code and pass on the fix.


I ran the full app locally (Java 17, Node 22) and checked search, the status filter, pagination and the 400 error for an invalid status.