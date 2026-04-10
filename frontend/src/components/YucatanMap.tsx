import { cn } from '@/lib/utils'

interface YucatanMapProps {
  zones: Array<{
    id: number
    nombre: string
    status: 'open' | 'warning'
  }>
  className?: string
}

const ZONE_POSITIONS: Record<string, { x: number; y: number }> = {
  'rio lagartos': { x: 75, y: 25 },
  'río lagartos': { x: 75, y: 25 },
  'progreso': { x: 55, y: 35 },
  'celestún': { x: 30, y: 30 },
  'san felipe': { x: 85, y: 20 },
  'el cigarral': { x: 65, y: 40 },
  'ría': { x: 45, y: 50 },
}

export function YucatanMap({ zones, className }: YucatanMapProps) {
  const getPosition = (nombre: string) => {
    const lower = nombre.toLowerCase()
    for (const [key, pos] of Object.entries(ZONE_POSITIONS)) {
      if (lower.includes(key)) {
        return pos
      }
    }
    return { x: 50, y: 50 }
  }

  return (
    <svg
      viewBox="0 0 100 100"
      className={cn('w-full h-full', className)}
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <linearGradient id="oceanGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#dde9ff" />
          <stop offset="100%" stopColor="#a3c9ff" />
        </linearGradient>
        <linearGradient id="landGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#eff3ff" />
          <stop offset="100%" stopColor="#d6e3fc" />
        </linearGradient>
      </defs>

      <rect x="0" y="0" width="100" height="100" fill="url(#oceanGradient)" />

      <path
        d="M 15 20 Q 25 15, 40 25 Q 55 35, 60 30 Q 70 25, 80 20 Q 90 25, 95 35 L 90 50 Q 80 55, 70 50 Q 60 60, 50 55 Q 40 65, 30 60 Q 20 55, 15 45 Z"
        fill="url(#landGradient)"
        stroke="#c2c6d1"
        strokeWidth="0.5"
      />

      <text x="50" y="45" textAnchor="middle" fontSize="4" fill="#424750" fontFamily="Inter, sans-serif">
        Yucatán
      </text>

      {zones.slice(0, 5).map((zone) => {
        const pos = getPosition(zone.nombre)
        return (
          <g key={zone.id}>
            <circle
              cx={pos.x}
              cy={pos.y}
              r="3"
              fill={zone.status === 'open' ? '#006a6a' : '#6e0105'}
              stroke="white"
              strokeWidth="0.5"
              className={zone.status === 'open' ? 'animate-pulse' : ''}
            />
            <text
              x={pos.x}
              y={pos.y + 6}
              textAnchor="middle"
              fontSize="2.5"
              fill="#424750"
              fontFamily="Inter, sans-serif"
              fontWeight="500"
            >
              {zone.nombre.split('(')[0].trim().substring(0, 12)}
            </text>
          </g>
        )
      })}

      <path
        d="M 0 70 Q 20 65, 40 75 Q 60 85, 80 80 Q 95 75, 100 70"
        fill="none"
        stroke="#93f2f2"
        strokeWidth="0.3"
        strokeDasharray="1,1"
        opacity="0.5"
      />
      <path
        d="M 0 80 Q 25 75, 50 82 Q 75 90, 100 85"
        fill="none"
        stroke="#93f2f2"
        strokeWidth="0.3"
        strokeDasharray="1,1"
        opacity="0.3"
      />
    </svg>
  )
}