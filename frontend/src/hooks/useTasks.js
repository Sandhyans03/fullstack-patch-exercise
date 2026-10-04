import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';

export function useTasks(query, status, page, pageSize) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // One controller per effect run. The cleanup aborts the previous request, so a slow
    // response for an old query can never overwrite the results of a newer one.
    const controller = new AbortController();

    setLoading(true);
    setError(null); // clear any error left over from a previous failed request

    fetchTasks({ query, status, page, pageSize, signal: controller.signal })
      .then((data) => {
        if (controller.signal.aborted) return;
        setTasks(data.items);
        setTotal(data.total);
        setLoading(false);
      })
      .catch((err) => {
        // An aborted request is expected (a newer one replaced it), not an error to show.
        if (err.name === 'AbortError' || controller.signal.aborted) return;
        setError(err.message);
        setTasks([]);
        setTotal(0);
        // Previously loading was never reset here, so the table stayed on
        // "Loading tasks..." forever and the error message could never render.
        setLoading(false);
      });

    return () => controller.abort();
  }, [query, status, page, pageSize]);

  return { tasks, total, loading, error };
}
