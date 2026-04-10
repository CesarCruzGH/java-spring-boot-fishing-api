import { Link, useLocation } from 'react-router-dom'
import { LayoutDashboard, Fish, Map, Scale } from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/' },
  { icon: Fish, label: 'Catalog', path: '/especies' },
  { icon: Map, label: 'Zones', path: '/zonas' },
  { icon: Scale, label: 'Rules', path: '/regulaciones' },
]

export function BottomNav() {
  const location = useLocation()

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 glass-nav border-t border-outline-variant/10 px-4 py-2 flex justify-around items-center z-50">
      {navItems.map((item) => {
        const isActive = location.pathname === item.path ||
          (item.path !== '/' && location.pathname.startsWith(item.path))
        return (
          <Link
            key={item.path}
            to={item.path}
            className={cn(
              'flex flex-col items-center gap-1 py-1 px-2 rounded-lg transition-colors',
              isActive ? 'text-primary' : 'text-on-surface-variant'
            )}
          >
            <item.icon className="h-5 w-5" />
            <span className="text-[10px] font-bold">{item.label}</span>
          </Link>
        )
      })}
    </nav>
  )
}