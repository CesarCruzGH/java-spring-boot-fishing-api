import { Outlet } from 'react-router-dom'
import { Sidebar } from '@/components/Sidebar'
import { BottomNav } from '@/components/BottomNav'

export function Layout() {
  return (
    <div className="min-h-dvh flex flex-col">
      <Sidebar />
      <main className="md:ml-64 min-h-screen flex flex-col pb-20 md:pb-0">
        <Outlet />
        <footer className="mt-auto border-t border-outline-variant/10 bg-surface-container-lowest py-6 px-8 flex flex-col md:flex-row justify-between items-center gap-4 text-xs font-medium text-on-surface-variant">
          <p>© 2024 Pesca Yucatán - Autoridad Marítima del Estado. Todos los derechos reservados.</p>
          <div className="flex gap-6">
            <a className="hover:text-primary transition-colors" href="#">Normatividad Federal</a>
            <a className="hover:text-primary transition-colors" href="#">Privacidad</a>
            <a className="hover:text-primary transition-colors" href="#">Manual de Usuario</a>
          </div>
        </footer>
      </main>
      <BottomNav />
    </div>
  )
}