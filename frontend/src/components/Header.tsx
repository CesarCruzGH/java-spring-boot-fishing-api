import { Link } from 'react-router-dom'
import { Fish, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { Button } from '@/components/ui/button'

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/80">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2">
          <Fish className="h-6 w-6 text-primary" />
          <span className="font-semibold text-lg text-text-primary">Pesca Yucatán</span>
        </Link>

        <nav className="hidden md:flex items-center gap-6">
          <Link to="/" className="text-sm font-medium text-text-secondary hover:text-text-primary transition-colors">
            Inicio
          </Link>
          <Link to="/especies" className="text-sm font-medium text-text-secondary hover:text-text-primary transition-colors">
            Especies
          </Link>
          <Link to="/zonas" className="text-sm font-medium text-text-secondary hover:text-text-primary transition-colors">
            Zonas
          </Link>
          <Link to="/admin">
            <Button variant="ghost" size="sm">Admin</Button>
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