package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Validate paging input up front: page < 1 made subList() throw (HTTP 500),
        // and an unbounded pageSize let one request ask for the whole table.
        if (page < 1) {
            return badRequest("page must be >= 1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return badRequest("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        // Normalize query input. Locale.ROOT avoids locale-specific case folding
        // (e.g. the Turkish dotless i) corrupting the search term or the enum name.
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + escapeLike(query.toLowerCase(Locale.ROOT)) + "%";

        // Parse status filter: an unknown value is a client error (400), not a server error (500).
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown status '" + status + "'. Allowed values: "
                        + Arrays.toString(TaskStatus.values()));
            }
        }

        log.debug("searchTasks q.length={} status={} page={} pageSize={}",
                query.length(), normalizedStatus, page, pageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        // long arithmetic so a huge page number cannot overflow int and wrap negative.
        long start = (long) (page - 1) * pageSize;
        List<Task> pageResults;
        if (start < allResults.size()) {
            int end = (int) Math.min(start + pageSize, allResults.size());
            pageResults = allResults.subList((int) start, end);
        } else {
            pageResults = Collections.emptyList();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }

    // '%' and '_' are LIKE wildcards. Escape them (and the escape char itself, backslash,
    // which is H2's default LIKE escape) so a user searching "100%" or "a_b" gets a literal match.
    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
