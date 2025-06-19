import { LuChevronLeft, LuChevronRight } from 'react-icons/lu';
import Button from '../ui/Button';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const Pagination: React.FC<PaginationProps> = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) {
    return null;
  }

  const currentPageForDisplay = currentPage + 1;

  const getPageNumbers = () => {
    const pages = [];
    const pageLimit = 5;
    const startPage = Math.max(1, currentPageForDisplay - 2);
    const endPage = Math.min(totalPages, startPage + pageLimit - 1);

    if (startPage > 1) {
      pages.push(1);
      if (startPage > 2) pages.push('...');
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    if (endPage < totalPages) {
      if (endPage < totalPages - 1) pages.push('...');
      pages.push(totalPages);
    }

    return pages;
  };

  const pageNumbers = getPageNumbers();

  return (
    <nav className="flex items-center justify-center mt-8" aria-label="Paginação">
      <Button
        variant="ghost"
        size="icon"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className="rounded-btn"
      >
        <LuChevronLeft className="h-4 w-4" />
      </Button>

      {pageNumbers.map((page, index) =>
        typeof page === 'string' ? (
          <span key={`ellipsis-${index}`} className="px-2 py-1 text-text-secondary">
            ...
          </span>
        ) : (
          <Button
            key={page}
            variant={currentPageForDisplay === page ? 'secondary' : 'ghost'}
            size="icon"
            onClick={() => onPageChange(page - 1)}
            className="rounded-btn p-1"
          >
            {page}
          </Button>
        )
      )}

      <Button
        variant="ghost"
        size="icon"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages - 1}
        className="rounded-btn"
      >
        <LuChevronRight className="h-4 w-4" />
      </Button>
    </nav>
  );
};
export default Pagination;
