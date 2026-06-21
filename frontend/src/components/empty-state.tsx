interface EmptyStateProps {
  readonly message: string;
  readonly action?: {
    readonly label: string;
    readonly onClick: () => void;
  };
}

export function EmptyState({ message, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <p className="text-muted-foreground">{message}</p>
      {action !== undefined && (
        <button type="button" onClick={action.onClick} className="mt-4 text-sm underline">
          {action.label}
        </button>
      )}
    </div>
  );
}
