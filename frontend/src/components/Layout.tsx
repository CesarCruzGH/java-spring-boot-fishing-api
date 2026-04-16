import { Outlet } from 'react-router-dom'
import { Header } from '@/components/Header'

export function Layout() {
  return (
    <div className="min-h-dvh flex flex-col">
      <Header />
      <main className="flex-1">
        <Outlet />
      </main>
      <footer style={{ backgroundColor: 'rgba(21, 21, 21)' }} className="border-t border-white/10 py-6 px-8 flex flex-col md:flex-row justify-between items-center gap-4 text-xs font-medium text-white/60">
        <p>© 2024 Pesca Yucatán - Autoridad Marítima del Estado. Todos los derechos reservados.</p>
        <div className="flex gap-6">
          <a className="hover:text-primary transition-colors" href="#">Normatividad Federal</a>
          <a className="hover:text-primary transition-colors" href="#">Privacidad</a>
          <a className="hover:text-primary transition-colors" href="#">Manual de Usuario</a>
        </div>
      </footer>
    </div>
  )
}