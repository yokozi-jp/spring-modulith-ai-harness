import { test, expect, APIRequestContext } from "@playwright/test";

const KEYCLOAK_TOKEN_URL =
  "http://keycloak:8080/realms/demo/protocol/openid-connect/token";

async function getAccessToken(request: APIRequestContext): Promise<string> {
  const response = await request.post(KEYCLOAK_TOKEN_URL, {
    form: {
      grant_type: "password",
      client_id: "demo-app",
      client_secret: "demo-app-secret",
      username: "testuser",
      password: "test",
    },
  });
  expect(response.ok()).toBeTruthy();
  const body = await response.json();
  return body.access_token as string;
}

test.describe("Category API CRUD", () => {
  let token: string;
  let csrfToken: string;
  let sessionCookie: string;

  test.beforeAll(async ({ request }) => {
    // OAuth2 Login flow: get session via form login
    // Since this is BFF pattern (session-based), we need to login through OAuth2
    // But for API testing, we use direct token approach with a workaround

    token = await getAccessToken(request);
  });

  test("POST /api/v1/categories creates a category and returns 201 with Location", async ({
    request,
  }) => {
    const response = await request.post("/api/v1/categories", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      data: {
        name: "テストカテゴリ",
        sortOrder: 1,
        parentCategoryId: null,
      },
    });

    expect(response.status()).toBe(201);
    const location = response.headers()["location"];
    expect(location).toBeTruthy();
    expect(location).toContain("/api/v1/categories/");
  });

  test("GET /api/v1/categories without auth returns redirect to login", async ({
    request,
  }) => {
    const response = await request.get("/api/v1/categories", {
      maxRedirects: 0,
    });

    // OAuth2 Login pattern: unauthenticated requests get 302 to Keycloak
    expect(response.status()).toBe(302);
    const location = response.headers()["location"];
    expect(location).toContain("keycloak");
  });
});
