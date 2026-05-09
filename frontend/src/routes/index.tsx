import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  component: IndexPage,
});

function IndexPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-8">
      <h1 className="text-4xl font-bold">Spring Modulith AI Harness</h1>
      <p className="text-muted-foreground">Frontend is ready.</p>
    </main>
  );
}
