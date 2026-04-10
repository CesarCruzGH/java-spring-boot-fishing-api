import { ChevronLeft, ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

interface PaginationProps {
  totalItems: number
  itemsPerPage: number
  currentPage: number
  onPageChange: (page: number) => void
  className?: string
}

export function Pagination({
  totalItems,
  itemsPerPage,
  currentPage,
  onPageChange,
  className,
}: PaginationProps) {
  const totalPages = Math.ceil(totalItems / itemsPerPage)
  
  if (totalPages <= 1) return null

  const pages = generatePages(currentPage, totalPages)

  function generatePages(current: number, total: number): (number | 'ellipsis')[] {
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1)
    }

    if (current <= 3) {
      return [1, 2, 3, 4, 5, 'ellipsis', total]
    }

    if (current >= total - 2) {
      return [1, 'ellipsis', total - 4, total - 3, total - 2, total - 1, total]
    }

    return [1, 'ellipsis', current - 1, current, current + 1, 'ellipsis', total]
  }

  return (
    <footer className={cn('flex justify-between items-center pt-8 border-t border-outline-variant/20', className)}>
      <div className="text-on-surface-variant text-sm font-medium">
        © 2024 Pesca Yucatán - Registro de Especies v2.4.1
      </div>
      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
          className={cn(
            'w-10 h-10 flex items-center justify-center rounded-lg transition-all',
            currentPage === 1
              ? 'bg-surface-container-low text-outline cursor-not-allowed'
              : 'bg-surface-container-high text-primary hover:bg-primary hover:text-on-primary'
          )}
        >
          <ChevronLeft className="h-5 w-5" />
        </button>

        {pages.map((page, index) =>
          page === 'ellipsis' ? (
            <span key={`ellipsis-${index}`} className="w-10 h-10 flex items-center justify-center text-outline">
              ...
            </span>
          ) : (
            <button
              key={page}
              type="button"
              onClick={() => onPageChange(page)}
              className={cn(
                'px-4 h-10 flex items-center justify-center rounded-lg font-bold text-sm transition-all',
                page === currentPage
                  ? 'bg-primary text-on-primary'
                  : 'bg-surface-container-lowest text-on-surface hover:bg-surface-container-high'
              )}
            >
              {page}
            </button>
          )
        )}

        <button
          type="button"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
          className={cn(
            'w-10 h-10 flex items-center justify-center rounded-lg transition-all',
            currentPage === totalPages
              ? 'bg-surface-container-low text-outline cursor-not-allowed'
              : 'bg-surface-container-high text-primary hover:bg-primary hover:text-on-primary'
          )}
        >
          <ChevronRight className="h-5 w-5" />
        </button>
      </div>
    </footer>
  )
}