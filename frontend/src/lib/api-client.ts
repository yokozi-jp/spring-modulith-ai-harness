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

  const headers: Record<string, string> = {};

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
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(config.data);
  }

  const response = await fetch(url.toString(), init);

  if (!response.ok) {
    const text = await response.text();
    let message = `${response.status} ${response.statusText}`;
    if (text.length > 0) {
      try {
        const problem = JSON.parse(text) as { detail?: string };
        if (problem.detail !== undefined) {
          message = problem.detail;
        }
      } catch {
        message = text;
      }
    }
    throw new Error(message);
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
