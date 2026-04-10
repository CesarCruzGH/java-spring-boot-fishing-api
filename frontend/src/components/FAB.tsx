import { Plus } from 'lucide-react'
import { cn } from '@/lib/utils'

interface FABProps {
  onClick?: () => void
  className?: string
}

export function FAB({ onClick, className }: FABProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        'fixed bottom-24 md:bottom-8 right-8 bg-primary text-white w-14 h-14 rounded-full flex items-center justify-center shadow-2xl hover:scale-105 transition-transform z-[70]',
        className
      )}
      aria-label="Add new item"
    >
      <Plus className="h-6 w-6" />
    </button>
  )
}