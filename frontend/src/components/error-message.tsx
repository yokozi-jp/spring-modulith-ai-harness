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
        <button type="button" onClick={onRetry} className="mt-2 text-sm underline">
          再試行
        </button>
      )}
    </div>
  );
}
