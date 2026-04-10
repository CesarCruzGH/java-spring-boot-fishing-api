import { Search, Bell, Settings } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'

interface TopBarProps {
  title: string
  subtitle?: string
}

export function TopBar({ title, subtitle }: TopBarProps) {
  return (
    <header className="bg-[#f8f9ff]/80 backdrop-blur-md sticky top-0 z-50 flex justify-between items-center px-8 py-4 w-full shadow-[0_20px_40px_rgba(14,28,46,0.06)]">
      <div className="flex items-center gap-8">
        <div className="flex items-center gap-3">
          <span className="text-xl font-black text-primary uppercase tracking-wider font-['Manrope']">
            {title}
          </span>
          {subtitle && (
            <span className="text-sm text-on-surface-variant hidden lg:block">
              {subtitle}
            </span>
          )}
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="relative hidden sm:block">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-outline" />
          <Input
            type="search"
            placeholder="Search species or zone..."
            className="pl-10 w-64"
          />
        </div>

        <Button variant="ghost" size="icon" aria-label="Notifications">
          <Bell className="h-5 w-5" />
        </Button>

        <Button variant="ghost" size="icon" aria-label="Settings">
          <Settings className="h-5 w-5" />
        </Button>
      </div>
    </header>
  )
}