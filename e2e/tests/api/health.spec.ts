import { test, expect } from "@playwright/test";

test.describe("Backend Health Check", () => {
  test("GET /actuator/health returns UP", async ({ request }) => {
    const response = await request.get("/actuator/health");

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.status).toBe("UP");
  });

  test("GET /actuator/health/readiness returns UP", async ({ request }) => {
    const response = await request.get("/actuator/health/readiness");

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.status).toBe("UP");
  });

  test("GET /actuator/health/liveness returns UP", async ({ request }) => {
    const response = await request.get("/actuator/health/liveness");

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.status).toBe("UP");
  });
});
