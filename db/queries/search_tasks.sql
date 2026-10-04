-- H2-compatible task search query
-- Used by the Spring Data repository layer
--
-- Parameters:
--   :term   — search term wrapped in wildcards, e.g. '%api%'
--   :status — status filter or NULL for all statuses

-- NOTE: the title/description OR must be parenthesised. AND binds tighter than OR,
-- so without the parentheses archived and status only constrained the title match.
-- id DESC is a tie-breaker so ordering is deterministic across pages.
SELECT *
FROM tasks
WHERE archived = FALSE
  AND (LOWER(title) LIKE :term OR LOWER(description) LIKE :term)
  AND (:status IS NULL OR status = :status)
ORDER BY created_at DESC, id DESC;
