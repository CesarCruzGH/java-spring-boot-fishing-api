import { Link } from 'react-router-dom'
import { Fish, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { MegaMenu } from '@/components/MegaMenu'

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <header className="absolute top-0 left-0 z-50 w-full border-b border-white/10 bg-transparent">
      <div className="container mx-auto flex h-16 items-center gap-8 px-4">
        <Link to="/" className="flex items-center gap-2 shrink-0">
          <Fish className="h-6 w-6 text-white" />
          <span className="font-semibold text-lg text-white">Pesca Yucatán</span>
        </Link>

        <nav className="hidden md:flex items-center gap-6">
          <Link to="/" className="text-sm font-medium text-white/80 hover:text-white transition-colors">
            Inicio
          </Link>
          <Link to="/especies" className="text-sm font-medium text-white/80 hover:text-white transition-colors">
            Especies
          </Link>
          <MegaMenu />
          <Link to="/zonas" className="text-sm font-medium text-white/80 hover:text-white transition-colors">
            Zonas
          </Link>
          <Link to="/admin">
            <Button variant="ghost" size="sm" className="text-white hover:bg-white/10">Admin</Button>
          </Link>
        </nav>

        <button
          type="button"
          className="md:hidden p-2 touch-target"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          {mobileMenuOpen ? (
            <X className="h-6 w-6 text-text-primary" />
          ) : (
            <Menu className="h-6 w-6 text-text-primary" />
          )}
        </button>
      </div>

      {mobileMenuOpen && (
        <nav className="md:hidden border-t border-border bg-white">
          <div className="container px-4 py-4 flex flex-col gap-4">
            <Link
              to="/"
              className="text-sm font-medium text-text-secondary hover:text-text-primary touch-target"
              onClick={() => setMobileMenuOpen(false)}
            >
              Inicio
            </Link>
            <Link
              to="/especies"
              className="text-sm font-medium text-text-secondary hover:text-text-primary touch-target"
              onClick={() => setMobileMenuOpen(false)}
            >
              Especies
            </Link>
            <Link
              to="/zonas"
              className="text-sm font-medium text-text-secondary hover:text-text-primary touch-target"
              onClick={() => setMobileMenuOpen(false)}
            >
              Zonas
            </Link>
            <Link
              to="/admin"
              className="text-sm font-medium text-text-secondary hover:text-text-primary touch-target"
              onClick={() => setMobileMenuOpen(false)}
            >
              Admin
            </Link>
          </div>
        </nav>
      )}
    </header>
  )
}