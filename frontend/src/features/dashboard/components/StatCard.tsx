import { ElementType } from 'react'
import { Card } from '@/shared/components/ui/Card'

interface StatCardProps {
  label: string
  value: string | number
  icon: ElementType
  sub?: string
  iconColor?: string
}

export function StatCard({ label, value, icon: Icon, sub, iconColor = 'text-brand-600' }: StatCardProps) {
  return (
    <Card className="flex items-start gap-4">
      <div className={`rounded-lg bg-brand-50 p-2.5 ${iconColor}`}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm text-slate-500">{label}</p>
        <p className="mt-0.5 text-2xl font-bold text-slate-900">{value}</p>
        {sub && <p className="mt-0.5 text-xs text-slate-400">{sub}</p>}
      </div>
    </Card>
  )
}
