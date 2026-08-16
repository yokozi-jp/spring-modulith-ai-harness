import { ApiError } from "@/lib/api-error";

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
    let title = response.statusText;
    let detail = "";
    if (text.length > 0) {
      try {
        const problem = JSON.parse(text) as { title?: string; detail?: string };
        const { title: problemTitle, detail: problemDetail } = problem;
        if (problemTitle !== undefined) {
          title = problemTitle;
        }
        if (problemDetail !== undefined) {
          detail = problemDetail;
        }
      } catch {
        detail = text;
      }
    }
    throw new ApiError(response.status, title, detail);
  }

  const text = await response.text();
  let data: unknown;
  if (text.length > 0) {
    try {
      data = JSON.parse(text);
    } catch {
      throw new Error(`Invalid JSON response: ${text.slice(0, 100)}`);
    }
  }

  // Orval 生成コードが期待する形式で返す
  return { data, status: response.status, headers: response.headers } as T;
}
