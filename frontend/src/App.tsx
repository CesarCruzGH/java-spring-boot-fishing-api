import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Layout } from '@/components/Layout'
import { Dashboard } from '@/pages/Dashboard'
import { SpeciesCatalog } from '@/pages/SpeciesCatalog'
import { SpeciesDetail } from '@/pages/SpeciesDetail'
import { Zonas } from '@/pages/Zonas'
import { ZoneDetail } from '@/pages/ZoneDetail'
import { Login } from '@/pages/Login'
import { AdminIngestion } from '@/pages/AdminIngestion'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: 1,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="especies" element={<SpeciesCatalog />} />
            <Route path="especies/:id" element={<SpeciesDetail />} />
            <Route path="zonas" element={<Zonas />} />
            <Route path="zonas/:id" element={<ZoneDetail />} />
          </Route>
          <Route path="/login" element={<Login />} />
          <Route path="/admin" element={<AdminIngestion />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App