import { defineConfig } from "orval";

export default defineConfig({
  api: {
    input: {
      target: "./openapi.json",
    },
    output: {
      target: "./src/api",
      client: "react-query",
      httpClient: "fetch",
      mode: "tags-split",
      clean: true,
      override: {
        mutator: {
          path: "./src/lib/api-client.ts",
          name: "apiClient",
        },
        // query オプションを削除してデフォルト動作に任せる:
        // - GET → useQuery
        // - POST/PUT/PATCH/DELETE → useMutation
      },
    },
  },
});
