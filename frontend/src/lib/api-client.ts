const BASE_URL = "/api/v1";

function getCsrfToken(): string | null {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  const token = match?.[1];
  return token !== undefined ? decodeURIComponent(token) : null;
}

export async function apiClient<T>(config: {
  url: string;
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  data?: unknown;
  params?: Record<string, string>;
}): Promise<T> {
  const url = new URL(`${BASE_URL}${config.url}`, window.location.origin);

  if (config.params) {
    for (const [key, value] of Object.entries(config.params)) {
      url.searchParams.set(key, value);
    }
  }

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  const csrfToken = getCsrfToken();
  if (csrfToken !== null) {
    headers["X-XSRF-TOKEN"] = csrfToken;
  }

  const init: RequestInit = {
    method: config.method,
    headers,
    credentials: "include",
  };

  if (config.data !== undefined) {
    init.body = JSON.stringify(config.data);
  }

  const response = await fetch(url.toString(), init);

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `${response.status} ${response.statusText}`);
  }

  if (response.status === 204 || response.status === 201) {
    return undefined as T;
  }

  const text = await response.text();
  if (text.length === 0) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}
