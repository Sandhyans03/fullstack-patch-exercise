const API_BASE = '/api';

export async function fetchTasks({ query = '', status = '', page = 1, pageSize = 10, signal }) {
  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (status) params.set('status', status);
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));

  const response = await fetch(`${API_BASE}/tasks?${params.toString()}`, { signal });

  if (!response.ok) {
    // The backend returns { error: "..." } for 400s; surface that instead of a bare status code.
    let detail = '';
    try {
      detail = (await response.json()).error || '';
    } catch {
      // body was not JSON; fall back to the status code below
    }
    throw new Error(detail || `Request failed: ${response.status}`);
  }

  return response.json();
}
