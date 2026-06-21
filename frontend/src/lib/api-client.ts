function getCsrfToken(): string | null {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  const token = match?.[1];
  return token !== undefined ? decodeURIComponent(token) : null;
}

export async function apiClient<T>(url: string, options?: RequestInit): Promise<T> {
  const headers = new Headers(options?.headers);

  const csrfToken = getCsrfToken();
  if (csrfToken !== null) {
    headers.set("X-XSRF-TOKEN", csrfToken);
  }

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: "include",
  });

  if (!response.ok) {
    const text = await response.text();
    let message = `${String(response.status)} ${response.statusText}`;
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

  const text = await response.text();
  const data = text.length > 0 ? JSON.parse(text) : undefined;

  // Orval 生成コードが期待する形式で返す
  return { data, status: response.status, headers: response.headers } as T;
}
