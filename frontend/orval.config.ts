import { defineConfig } from "orval";

export default defineConfig({
  api: {
    input: {
      target: "http://localhost:18080/v3/api-docs",
    },
    output: {
      target: "./src/api",
      client: "react-query",
      mode: "tags-split",
      clean: true,
      override: {
        mutator: {
          path: "./src/lib/api-client.ts",
          name: "apiClient",
        },
        query: {
          useQuery: true,
          useMutation: true,
        },
      },
    },
  },
});
