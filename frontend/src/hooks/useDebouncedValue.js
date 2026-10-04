import { useState, useEffect } from 'react';

// Returns `value`, but only after it has stopped changing for `delay` ms.
// Used so typing in the search box sends one request per pause, not one per keystroke.
export function useDebouncedValue(value, delay = 300) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}
