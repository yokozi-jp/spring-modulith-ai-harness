const BASE_URL = "/api";

export async function apiClient<T>(config: {
  url: string;
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  data?: unknown;
  params?: Record<string, string>;
  headers?: Record<string, string>;
}): Promise<T> {
  const url = new URL(`${BASE_URL}${config.url}`, window.location.origin);

  if (config.params) {
    for (const [key, value] of Object.entries(config.params)) {
      url.searchParams.set(key, value);
    }
  }

  const init: RequestInit = {
    method: config.method,
    headers: {
      "Content-Type": "application/json",
      ...config.headers,
    },
    credentials: "include",
  };

  if (config.data) {
    init.body = JSON.stringify(config.data);
  }

  const response = await fetch(url.toString(), init);

  if (!response.ok) {
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
