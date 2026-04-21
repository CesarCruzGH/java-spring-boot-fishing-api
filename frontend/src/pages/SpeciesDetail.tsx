import { useParams, Link } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { Fish, ExternalLink } from 'lucide-react'
import { usePez, usePeriodosVedaByPez, useRegulacionesByPezDetalle } from '@/api/hooks'

function formatMes(mes: number): string {
  const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']
  return meses[mes - 1] ?? mes.toString()
}

function formatMesCompleto(mes: number): string {
  const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']
  return meses[mes - 1] ?? mes.toString()
}

function getVedaStatus(periodos: any[], mesActual: number): { isOpen: boolean; activeVeda: any | null } {
  const today = new Date()
  const diaActual = today.getDate()

  for (const periodo of periodos) {
    const tipo = periodo.tipoVeda

    if (tipo === 'PERMANENTE') {
      return { isOpen: false, activeVeda: periodo }
    }

    const mesInicio = periodo.mesInicio
    const mesFin = periodo.mesFin
    const diaInicio = periodo.diaInicio ?? 1
    const diaFin = periodo.diaFin ?? 28

    if (mesInicio <= mesFin) {
      const isActive = (mesActual > mesInicio || (mesActual === mesInicio && diaActual >= diaInicio)) &&
        (mesActual < mesFin || (mesActual === mesFin && diaActual <= diaFin))
      if (isActive) {
        return { isOpen: false, activeVeda: periodo }
      }
    } else {
      const isActive = (mesActual > mesInicio || (mesActual === mesInicio && diaActual >= diaInicio)) ||
        (mesActual < mesFin || (mesActual === mesFin && diaActual <= diaFin))
      if (isActive) {
        return { isOpen: false, activeVeda: periodo }
      }
    }
  }

  return { isOpen: true, activeVeda: null }
}

const SECTIONS = [
  { id: 'biologia', label: 'Biología' },
  { id: 'vedas', label: 'Vedas' },
  { id: 'regulaciones', label: 'Zonas y Regulaciones' },
  { id: 'artes', label: 'Artes de Pesca' },
]

export function SpeciesDetail() {
  const { id } = useParams<{ id: string }>()
  const pezId = Number(id)

  const { data: pez, isLoading: loadingPez } = usePez(pezId)
  const { data: periodosVeda } = usePeriodosVedaByPez(pezId)
  const { data: regulaciones } = useRegulacionesByPezDetalle(pezId)

  const [scrollProgress, setScrollProgress] = useState(0)
  const [activeSection, setActiveSection] = useState('biologia')

  const today = new Date()
  const mesActual = today.getMonth() + 1
  const { isOpen, activeVeda } = periodosVeda ? getVedaStatus(periodosVeda, mesActual) : { isOpen: true, activeVeda: null }

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY
      const docHeight = document.documentElement.scrollHeight - window.innerHeight
      const progress = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0
      setScrollProgress(progress)

      const headerOffset = 150
      const scrollPosition = scrollTop + headerOffset + 100

      for (let i = SECTIONS.length - 1; i >= 0; i--) {
        const section = SECTIONS[i]
        const element = document.getElementById(section.id)
        if (element) {
          const offsetTop = element.offsetTop
          if (scrollPosition >= offsetTop) {
            setActiveSection(section.id)
            break
          }
        }
      }
    }

    window.addEventListener('scroll', handleScroll, { passive: true })
    handleScroll()
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId)
    if (element) {
      const headerOffset = 150
      const elementPosition = element.getBoundingClientRect().top
      const offsetPosition = elementPosition + window.scrollY - headerOffset

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth',
      })
    }
  }

  if (loadingPez) {
    return (
      <div className="min-h-screen bg-surface-container-lowest">
        <div className="animate-pulse">
          <div className="h-64 bg-surface-container" />
          <div className="max-w-screen-xl mx-auto px-6 py-16">
            <div className="h-8 bg-surface-container-low rounded w-1/4 mb-4" />
            <div className="h-4 bg-surface-container-low rounded w-1/2" />
          </div>
        </div>
      </div>
    )
  }

  if (!pez) {
    return (
      <div className="min-h-screen bg-surface-container-lowest flex items-center justify-center">
        <div className="text-center">
          <Fish className="h-12 w-12 mx-auto text-on-surface-variant/50 mb-3" />
          <p className="text-on-surface-variant">Especie no encontrada</p>
          <Link to="/especies" className="text-primary hover:underline mt-4 inline-block">
            Volver al catálogo
          </Link>
        </div>
      </div>
    )
  }

  const mesesTrimestres = [
    [1, 2, 3],
    [4, 5, 6],
    [7, 8, 9],
    [10, 11, 12],
  ]

  return (
    <div className="min-h-screen bg-surface-container-lowest">
      {/* Progress Bar */}
      <div className="fixed top-0 left-0 w-full h-1 z-[60] bg-surface-container-lowest/50">
        <div
          className="h-full bg-gradient-to-r from-primary to-tertiary transition-all duration-150 ease-out"
          style={{ width: `${scrollProgress}%` }}
        />
      </div>

      {/* Hero Section */}
      <section className="w-full bg-surface-container-low border-b border-outline-variant/15 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-tr from-surface to-surface-container/50 pointer-events-none" />
        <div className="max-w-screen-xl mx-auto px-6 lg:px-12 py-16 lg:py-24 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center relative z-10">
          <div className="flex flex-col gap-6 order-2 lg:order-1">
            <div className="inline-flex items-center self-start gap-2 px-4 py-2 bg-[#22c55e]/10 border border-[#22c55e]/30 rounded-full">
              <div className={`w-2 h-2 rounded-full ${isOpen ? 'bg-[#22c55e]' : 'bg-error'} animate-pulse`} />
              <span className="font-headline font-bold text-sm tracking-wider text-[#22c55e] uppercase">
                {isOpen ? 'Pesca Abierta' : 'En Veda'}
              </span>
            </div>
            <div className="flex flex-col gap-1">
              <h1 className="font-headline text-5xl lg:text-7xl font-extrabold text-on-surface tracking-tight">
                {pez.nombreComun}
              </h1>
              <p className="font-headline text-2xl text-secondary italic">{pez.nombreCientifico}</p>
            </div>
            {pez.nombreMaya && (
              <div className="flex items-center gap-3 text-on-surface-variant font-body">
                <span className="material-symbols-outlined text-outline">translate</span>
                <span className="text-lg">Maya: <span className="text-on-surface font-semibold">{pez.nombreMaya}</span></span>
              </div>
            )}
          </div>
          <div className="order-1 lg:order-2 relative aspect-[4/3] rounded-xl overflow-hidden bg-surface-container shadow-2xl">
            {pez.imagenUrl ? (
              <img
                src={pez.imagenUrl}
                alt={pez.nombreComun}
                className="w-full h-full object-cover opacity-90 object-center"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-surface-container">
                <Fish className="h-24 w-24 text-on-surface-variant/30" />
              </div>
            )}
            <div className="absolute inset-0 bg-gradient-to-t from-surface-container via-transparent to-transparent opacity-80" />
          </div>
        </div>
      </section>

      {/* Sticky Tabs Navigation */}
      <div className="w-full bg-surface-container/90 backdrop-blur-md sticky top-[80px] z-50 border-b border-outline-variant/15">
        <div className="max-w-screen-xl mx-auto px-6 overflow-x-auto no-scrollbar">
          <nav className="flex gap-8 whitespace-nowrap py-4" role="tablist">
            {SECTIONS.map((section) => {
              const isActive = activeSection === section.id
              const sectionIndex = SECTIONS.findIndex(s => s.id === section.id)
              const activeIndex = SECTIONS.findIndex(s => s.id === activeSection)
              const isPast = sectionIndex < activeIndex

              return (
                <button
                  key={section.id}
                  onClick={() => scrollToSection(section.id)}
                  role="tab"
                  aria-selected={isActive}
                  className={`
                    font-headline font-bold tracking-wide pb-1 transition-all duration-300 relative
                    ${isActive ? 'text-primary' : 'text-on-surface-variant hover:text-on-surface'}
                  `}
                >
                  <span className="relative z-10">{section.label}</span>
                  {isActive && (
                    <span className="absolute bottom-0 left-0 w-full h-0.5 bg-primary rounded-full" />
                  )}
                  {isPast && !isActive && (
                    <span className="absolute bottom-0 left-0 w-full h-0.5 bg-primary/30 rounded-full" />
                  )}
                </button>
              )
            })}
          </nav>
        </div>
      </div>

      <div className="max-w-screen-xl mx-auto px-6 lg:px-12 py-16 flex flex-col gap-24">
        {/* Biología y Hábitat */}
        <section className="scroll-mt-32" id="biologia">
          <div className="flex items-center gap-4 mb-8">
            <span className="material-symbols-outlined text-3xl text-primary">science</span>
            <h2 className="font-headline text-3xl font-bold text-on-surface">Biología y Hábitat</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="md:col-span-2 bg-surface-container-low rounded-xl p-8 border border-outline-variant/10 relative">
              <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 blur-3xl rounded-full pointer-events-none" />
              <p className="font-body text-lg text-on-surface-variant leading-relaxed">
                {pez.descripcion || 'Sin descripción disponible.'}
              </p>
            </div>
            <div className="flex flex-col gap-4">
              <div className="bg-surface-container rounded-lg p-5 flex flex-col gap-2">
                <span className="font-label text-xs tracking-widest text-outline uppercase">Hábitat</span>
                <div className="flex flex-wrap gap-2">
                  <span className="px-3 py-1 bg-tertiary-container/10 text-tertiary-dim rounded-md text-sm font-medium border border-tertiary-container/20 capitalize">
                    {pez.tipoAgua?.toLowerCase() || 'No especificado'}
                  </span>
                </div>
              </div>
              <div className="bg-surface-container rounded-lg p-5 flex flex-col gap-3">
                <span className="font-label text-xs tracking-widest text-outline uppercase">Avisos</span>
                <div className="flex items-center justify-between border-b border-outline-variant/10 pb-2">
                  <span className="font-body text-sm text-on-surface-variant">Ciguatera</span>
                  <span className={`font-body text-sm font-semibold ${pez.riesgoCiguatera === 'ALTO' ? 'text-error' : pez.riesgoCiguatera === 'MEDIO' ? 'text-warning' : 'text-[#22c55e]'}`}>
                    {pez.riesgoCiguatera || 'Sin riesgo'}
                  </span>
                </div>
                <div className="flex items-center justify-between border-b border-outline-variant/10 pb-2">
                  <span className="font-body text-sm text-on-surface-variant">Especie Protegida</span>
                  <span className="font-body text-sm text-on-surface font-semibold">
                    {pez.esProtegida ? 'Sí' : 'No'}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-body text-sm text-on-surface-variant">Invasora</span>
                  <span className="font-body text-sm text-on-surface font-semibold">
                    {pez.esInvasiva ? 'Sí' : 'No'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Calendario de Vedas */}
        <section className="scroll-mt-32" id="vedas">
          <div className="flex items-center gap-4 mb-8">
            <span className="material-symbols-outlined text-3xl text-error-dim">event_busy</span>
            <h2 className="font-headline text-3xl font-bold text-on-surface">Calendario de Vedas</h2>
          </div>
          <div className="bg-surface-container-low rounded-xl p-8 border border-outline-variant/10">
            {activeVeda ? (
              <p className="font-body text-on-surface-variant mb-4">
                Veda {activeVeda.tipoVeda === 'PERMANENTE' ? 'permanente' : 'temporal'} vigente{' '}
                {activeVeda.mesInicio && activeVeda.mesFin
                  ? `del ${activeVeda.diaInicio || 1} de ${formatMesCompleto(activeVeda.mesInicio)} al ${activeVeda.diaFin || 28} de ${formatMesCompleto(activeVeda.mesFin)}`
                  : ''}
                .
              </p>
            ) : (
              <p className="font-body text-on-surface-variant mb-4">
                No hay vedas activas en este momento.
              </p>
            )}
            <div className="flex justify-end gap-4 mb-4">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-[#22c55e]/20 border border-[#22c55e]" />
                <span className="text-xs font-label text-outline uppercase tracking-wider">Abierta</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-error/20 border border-error" />
                <span className="text-xs font-label text-outline uppercase tracking-wider">Cerrada</span>
              </div>
            </div>
            <div className="w-full bg-surface-container-lowest rounded-lg h-12 flex relative overflow-hidden border border-outline-variant/20">
              {mesesTrimestres.map((trimestre, idx) => {
                const isInVeda = trimestre.some(mes => {
                  if (activeVeda?.tipoVeda === 'PERMANENTE') return true
                  if (!activeVeda) return false
                  const mesInicio = activeVeda.mesInicio
                  const mesFin = activeVeda.mesFin
                  if (mesInicio <= mesFin) {
                    return mes >= mesInicio && mes <= mesFin
                  } else {
                    return mes >= mesInicio || mes <= mesFin
                  }
                })
                return (
                  <div
                    key={idx}
                    className={`flex-1 h-full border-r border-outline-variant/20 flex items-center justify-center relative group ${isInVeda ? 'bg-error/20' : 'bg-[#22c55e]/10'}`}
                  >
                    {isInVeda && (
                      <div
                        className="absolute inset-0 opacity-20"
                        style={{ backgroundImage: 'repeating-linear-gradient(45deg, transparent, transparent 10px, #ffb4ab 10px, #ffb4ab 20px)' }}
                      />
                    )}
                    <span className={`text-xs font-bold relative z-10 ${isInVeda ? 'text-error' : 'text-[#22c55e]'}`}>
                      {formatMes(trimestre[0])} - {formatMes(trimestre[2])}
                    </span>
                  </div>
                )
              })}
            </div>
          </div>
        </section>

        {/* Regulaciones por Zona */}
        <section className="scroll-mt-32" id="regulaciones">
          <div className="flex items-center gap-4 mb-8">
            <span className="material-symbols-outlined text-3xl text-secondary">policy</span>
            <h2 className="font-headline text-3xl font-bold text-on-surface">Regulaciones por Zona</h2>
          </div>
          {regulaciones && regulaciones.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {regulaciones.map((reg) => (
                <div key={reg.id} className="bg-surface-container-low rounded-xl border border-outline-variant/15 overflow-hidden flex flex-col">
                  <div className="bg-surface-container p-5 border-b border-outline-variant/15 flex justify-between items-center">
                    <h3 className="font-headline text-xl font-bold text-on-surface">{reg.zonaNombre}</h3>
                    <span className="px-3 py-1 bg-surface-container-highest rounded-full text-xs font-medium text-on-surface-variant border border-outline-variant/30 capitalize">
                      {reg.categoriaPesca?.toLowerCase().replace('_', ' / ') || 'No especificada'}
                    </span>
                  </div>
                  <div className="p-6 grid grid-cols-2 gap-6 flex-grow">
                    <div className="flex flex-col gap-1">
                      <span className="font-label text-xs tracking-widest text-outline uppercase flex items-center gap-1">
                        <span className="material-symbols-outlined text-[16px]">straighten</span> Talla Min.
                      </span>
                      <div className="flex items-baseline gap-1">
                        <span className="font-headline text-3xl font-bold text-primary">
                          {reg.tallaMinima ?? '-'}
                        </span>
                        <span className="text-on-surface-variant">cm</span>
                      </div>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="font-label text-xs tracking-widest text-outline uppercase flex items-center gap-1">
                        <span className="material-symbols-outlined text-[16px]">straighten</span> Talla Max.
                      </span>
                      <div className="flex items-baseline gap-1">
                        <span className="font-headline text-3xl font-bold text-primary">
                          {reg.tallaMaxima ?? '-'}
                        </span>
                        <span className="text-on-surface-variant">cm</span>
                      </div>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="font-label text-xs tracking-widest text-outline uppercase flex items-center gap-1">
                        <span className="material-symbols-outlined text-[16px]">scale</span> Cuota Diaria
                      </span>
                      <div className="flex items-baseline gap-1">
                        <span className="font-headline text-3xl font-bold text-on-surface">
                          {reg.cuotaDiaria ?? '-'}
                        </span>
                        <span className="text-on-surface-variant text-sm">ejemplares</span>
                      </div>
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="font-label text-xs tracking-widest text-outline uppercase flex items-center gap-1">
                        <span className="material-symbols-outlined text-[16px]">badge</span> Permiso
                      </span>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="px-2 py-1 bg-surface-variant text-on-surface rounded text-sm font-semibold">
                          {reg.requierePermiso ? 'Requerido' : 'No requerido'}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
              <div className="bg-surface-container-lowest rounded-xl border border-outline-variant/10 relative overflow-hidden hidden md:block min-h-[200px]">
                <div className="absolute inset-0 flex items-center justify-center flex-col gap-2 text-outline">
                  <span className="material-symbols-outlined text-4xl">map</span>
                  <span className="font-body text-sm">Mapa de Zonas de Pesca</span>
                </div>
              </div>
            </div>
          ) : (
            <p className="text-on-surface-variant">No hay regulaciones disponibles para esta especie.</p>
          )}
        </section>

        {/* Artes de Pesca */}
        <section className="scroll-mt-32" id="artes">
          <div className="flex items-center gap-4 mb-8">
            <span className="material-symbols-outlined text-3xl text-tertiary">phishing</span>
            <h2 className="font-headline text-3xl font-bold text-on-surface">Artes de Pesca Permitidas</h2>
          </div>
          {regulaciones && regulaciones.some(r => r.artesPesca && r.artesPesca.length > 0) ? (
            <div className="bg-surface-container-low rounded-xl p-6 border border-outline-variant/15">
              <ul className="flex flex-col divide-y divide-outline-variant/10">
                {regulaciones.flatMap(reg =>
                  (reg.artesPesca || []).filter(ap => !ap.esProhibido).map(ap => (
                    <li key={ap.id} className="py-4 flex items-center gap-4">
                      <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                        <span className="material-symbols-outlined text-primary text-sm">check</span>
                      </div>
                      <span className="font-body text-lg text-on-surface">{ap.nombre}</span>
                    </li>
                  ))
                )}
              </ul>
            </div>
          ) : (
            <p className="text-on-surface-variant">No hay información de artes de pesca disponible.</p>
          )}
        </section>
      </div>

      {/* Footer CTA */}
      <div className="bg-gradient-to-t from-surface-container-low to-surface border-t border-outline-variant/10 py-12 px-6">
        <div className="max-w-screen-xl mx-auto flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="flex flex-col gap-2 text-center md:text-left">
            <h3 className="font-headline text-2xl font-bold text-on-surface">Marco Legal</h3>
            <p className="font-body text-on-surface-variant max-w-lg">
              Consulta el Diario Oficial de la Federación para ver el documento normativo completo de esta especie.
            </p>
          </div>
          <button className="bg-gradient-to-r from-primary to-tertiary text-on-primary font-headline font-bold py-3 px-8 rounded-lg shadow-[0_0_20px_rgba(0,220,229,0.3)] hover:shadow-[0_0_30px_rgba(0,220,229,0.5)] transition-all transform hover:-translate-y-0.5 flex items-center gap-2">
            <span>Consultar DOF Oficial</span>
            <ExternalLink className="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>
  )
}