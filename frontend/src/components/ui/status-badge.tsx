import { CheckCircle, XCircle, AlertTriangle } from 'lucide-react'
import { cn } from '@/lib/utils'

interface StatusBadgeProps {
  status: 'open' | 'closed' | 'warning'
  label?: string
  showIcon?: boolean
  className?: string
}

export function StatusBadge({
  status,
  label,
  showIcon = true,
  className
}: StatusBadgeProps) {
  const config = {
    open: {
      icon: CheckCircle,
      variant: 'open',
      defaultLabel: 'Abierta'
    },
    closed: {
      icon: XCircle,
      variant: 'closed',
      defaultLabel: 'En Veda'
    },
    warning: {
      icon: AlertTriangle,
      variant: 'warning',
      defaultLabel: 'Precaución'
    }
  }

  const { icon: Icon, variant, defaultLabel } = config[status]

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-bold',
        variant === 'open' && 'bg-secondary-container text-on-secondary-container',
        variant === 'closed' && 'bg-tertiary-fixed text-on-tertiary-fixed-variant',
        variant === 'warning' && 'bg-warning/10 text-warning',
        className
      )}
    >
      {showIcon && <Icon className="h-3.5 w-3.5" />}
      {label || defaultLabel}
    </span>
  )
}