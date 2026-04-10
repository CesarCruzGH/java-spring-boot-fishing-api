import { Link, useLocation } from 'react-router-dom'
import {
  LayoutDashboard,
  Fish,
  Map,
  Scale,
  Database,
  HelpCircle,
  Archive,
  AlertTriangle
} from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/' },
  { icon: Fish, label: 'Species Catalog', path: '/especies' },
  { icon: Map, label: 'Zones & Maps', path: '/zonas' },
  { icon: Scale, label: 'Regulations', path: '/regulaciones' },
  { icon: Database, label: 'Data Pipeline', path: '/admin' },
]

const bottomItems = [
  { icon: HelpCircle, label: 'Support', path: '/support' },
  { icon: Archive, label: 'Archive', path: '/archive' },
]

export function Sidebar() {
  const location = useLocation()

  return (
    <aside className="hidden md:flex h-screen w-64 fixed left-0 top-0 z-[60] bg-[#0e1c2e] flex-col py-8">
      <div className="px-6 mb-10">
        <h1 className="text-white font-['Manrope'] font-extrabold tracking-tighter text-2xl">
          Maritime Authority
        </h1>
        <p className="text-[#d6e3fc]/60 text-xs font-['Inter'] uppercase tracking-widest mt-1">
          Yucatán Regulatory Portal
        </p>
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path ||
            (item.path !== '/' && location.pathname.startsWith(item.path))
          return (
            <Link
              key={item.path}
              to={item.path}
              className={cn(
                'rounded-r-full mr-4 px-6 py-3 font-semibold flex items-center gap-3 transition-all duration-150',
                isActive
                  ? 'bg-[#003461] text-white translate-x-1'
                  : 'text-[#d6e3fc]/70 hover:text-white hover:bg-[#003461]/50'
              )}
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </Link>
          )
        })}
      </nav>

      <div className="px-6 mt-auto space-y-6">
        <Link
          to="/report"
          className="w-full bg-[#6e0105] text-white py-3 rounded-lg font-bold text-sm hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
        >
          <AlertTriangle className="h-4 w-4" />
          Report Violation
        </Link>

        <div className="pt-6 border-t border-white/10 space-y-2">
          {bottomItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className="text-[#d6e3fc]/70 hover:text-white flex items-center gap-3 text-sm"
            >
              <item.icon className="h-4 w-4" />
              {item.label}
            </Link>
          ))}
        </div>
      </div>
    </aside>
  )
}