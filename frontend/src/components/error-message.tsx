import { Button } from "@/components/ui/button";

interface ErrorMessageProps {
  readonly error: Error | null;
  readonly onRetry?: () => void;
}

export function ErrorMessage({ error, onRetry }: ErrorMessageProps) {
  if (error === null) {
    return null;
  }

  return (
    <div role="alert" className="rounded-md border border-destructive/50 p-4">
      <p className="text-sm text-destructive">{error.message}</p>
      {onRetry !== undefined && (
        <Button variant="link" size="sm" onClick={onRetry} className="mt-2 h-auto p-0">
          再試行
        </Button>
      )}
    </div>
  );
}
