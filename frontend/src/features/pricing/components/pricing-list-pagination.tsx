import { Button } from "@/components/ui/button";

interface PricingListPaginationProps {
  readonly page: number;
  readonly totalPages: number;
  readonly onPageChange: (page: number) => void;
}

/** 価格一覧のページ送り。 */
export function PricingListPagination({
  page,
  totalPages,
  onPageChange,
}: PricingListPaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  function handlePrevious() {
    onPageChange(page - 1);
  }

  function handleNext() {
    onPageChange(page + 1);
  }

  return (
    <div className="flex items-center justify-between">
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={handlePrevious}
        disabled={page <= 0}
      >
        前へ
      </Button>
      <span className="text-sm text-muted-foreground">
        {page + 1} / {totalPages} ページ
      </span>
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={handleNext}
        disabled={page >= totalPages - 1}
      >
        次へ
      </Button>
    </div>
  );
}
