import { clsx } from 'clsx'

interface ProgressBarProps {
  value: number
  max: number
  label?: string
  showPercent?: boolean
  color?: 'brand' | 'green' | 'yellow' | 'red'
  className?: string
}

const trackColors = {
  brand: 'bg-brand-600',
  green: 'bg-green-500',
  yellow: 'bg-yellow-500',
  red: 'bg-red-500',
}

export function ProgressBar({
  value,
  max,
  label,
  showPercent,
  color = 'brand',
  className,
}: ProgressBarProps) {
  const pct = max === 0 ? 0 : Math.min(100, Math.round((value / max) * 100))
  const barColor =
    pct >= 90 ? 'bg-red-500' : pct >= 70 ? 'bg-yellow-500' : trackColors[color]

  return (
    <div className={clsx('space-y-1', className)}>
      {(label || showPercent) && (
        <div className="flex justify-between text-xs text-slate-600">
          {label && <span>{label}</span>}
          {showPercent && <span>{pct}%</span>}
        </div>
      )}
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
        <div
          className={clsx('h-full rounded-full transition-all', barColor)}
          style={{ width: `${pct}%` }}
          role="progressbar"
          aria-valuenow={value}
          aria-valuemax={max}
        />
      </div>
    </div>
  )
}
