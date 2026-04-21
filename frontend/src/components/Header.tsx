import { Link, useLocation } from 'react-router-dom'
import { Waves } from 'lucide-react'
import { MegaMenu } from '@/components/MegaMenu'
import { cn } from '@/lib/utils'

export function Header() {
  const location = useLocation()

  const isActive = (path: string) => location.pathname === path

  return (
    <header className="fixed top-0 w-full z-50 bg-[#000516]/60 backdrop-blur-xl shadow-[0_4px_30px_rgba(0,220,229,0.08)]">
      <div className="flex justify-between items-center px-12 py-6 w-full max-w-screen-2xl mx-auto">
        <Link to="/" className="flex items-center gap-3 shrink-0">
          <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-primary to-tertiary flex items-center justify-center shadow-[0_0_15px_rgba(0,220,229,0.4)]">
            <Waves className="h-5 w-5 text-[#001d33]" />
          </div>
          <span className="text-2xl font-black text-primary tracking-tighter font-headline">Pesca Yucatán</span>
        </Link>

        <nav className="hidden md:flex items-center gap-10 font-headline tracking-tight font-bold uppercase text-sm">
          <Link 
            to="/" 
            className={cn(
              'pb-2 transition-colors duration-200',
              isActive('/') ? 'text-white' : 'text-[#dae2fc]/70 hover:text-white'
            )}
          >
            Inicio
          </Link>
          <Link 
            to="/especies" 
            className={cn(
              'pb-2 transition-colors duration-200',
              isActive('/especies') ? 'text-tertiary border-b-2 border-tertiary bio-glow' : 'text-[#dae2fc]/70 hover:text-white'
            )}
          >
            Especies
          </Link>
          <MegaMenu />
          <Link 
            to="/zonas" 
            className={cn(
              'pb-2 transition-colors duration-200',
              isActive('/zonas') ? 'text-white' : 'text-[#dae2fc]/70 hover:text-white'
            )}
          >
            Zonas
          </Link>
        </nav>

        <div className="flex items-center gap-6">
          <Link 
            to="/admin"
            className="flex items-center gap-2 pl-2 pr-4 py-2 rounded-full bg-surface-container-low border border-outline-variant/20 hover:border-tertiary/40 transition-all group"
          >
            <span className="material-symbols-outlined text-primary text-lg">account_circle</span>
            <span className="text-xs font-headline font-bold uppercase tracking-wider text-on-surface-variant group-hover:text-tertiary transition-colors">Portal</span>
          </Link>
        </div>
      </div>

      <div className="bg-gradient-to-b from-surface-container-high to-transparent h-[1px] w-full absolute bottom-0" />
    </header>
  )
}