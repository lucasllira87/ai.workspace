import { formatDistanceToNow } from 'date-fns'
import { Card } from '@/shared/components/ui/Card'
import { Badge } from '@/shared/components/ui/Badge'
import type { RecentActivity as Activity } from '@/shared/api/types'

function moduleColor(module: string): 'indigo' | 'green' | 'blue' | 'gray' {
  const map: Record<string, 'indigo' | 'green' | 'blue' | 'gray'> = {
    DOCUMENTS: 'indigo',
    BILLING: 'green',
    LEARNING: 'blue',
  }
  return map[module.toUpperCase()] ?? 'gray'
}

export function RecentActivity({ activities }: { activities: Activity[] }) {
  return (
    <Card padding="none">
      <div className="border-b border-slate-200 px-6 py-4">
        <h3 className="font-semibold text-slate-900">Recent Activity</h3>
      </div>
      {activities.length === 0 ? (
        <p className="px-6 py-8 text-center text-sm text-slate-400">No recent activity</p>
      ) : (
        <ul className="divide-y divide-slate-100">
          {activities.map((a) => (
            <li key={a.id} className="flex items-center gap-4 px-6 py-3">
              <Badge color={moduleColor(a.module)}>{a.module}</Badge>
              <span className="flex-1 truncate text-sm text-slate-700">
                {a.eventType.replace(/_/g, ' ').toLowerCase()}
              </span>
              <span className="whitespace-nowrap text-xs text-slate-400">
                {formatDistanceToNow(new Date(a.occurredAt), { addSuffix: true })}
              </span>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}
