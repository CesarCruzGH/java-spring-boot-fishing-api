import * as React from 'react'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const badgeVariants = cva(
  'inline-flex items-center rounded-full px-3 py-1 text-xs font-bold tracking-widest uppercase',
  {
    variants: {
      variant: {
        open: 'bg-secondary-container text-on-secondary-container',
        closed: 'bg-tertiary-fixed text-on-tertiary-fixed-variant',
        warning: 'bg-warning/10 text-warning',
        default: 'bg-surface-container-low text-on-surface-variant border border-outline-variant',
        primary: 'bg-primary text-on-primary',
        secondary: 'bg-secondary text-on-secondary',
        tertiary: 'bg-tertiary text-on-tertiary',
        'secondary-container': 'bg-secondary-container text-on-secondary-container',
        'tertiary-container': 'bg-tertiary-container text-on-tertiary-container',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
)

type BadgeVariant = VariantProps<typeof badgeVariants>

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: BadgeVariant['variant']
}

function Badge({ className, variant = 'default', ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  )
}

export { Badge, badgeVariants }