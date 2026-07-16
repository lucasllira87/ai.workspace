import { useMutation, useQueryClient } from '@tanstack/react-query'
import { formatDistanceToNow } from 'date-fns'
import { Check } from 'lucide-react'
import { Spinner } from '@/shared/components/ui/Spinner'
import { markAsRead } from '../api'
import type { Notification } from '../types'

interface NotificationListProps {
  notifications: Notification[]
  isLoading: boolean
}

export function NotificationList({ notifications, isLoading }: NotificationListProps) {
  const queryClient = useQueryClient()

  const { mutate } = useMutation({
    mutationFn: markAsRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  })

  if (isLoading) {
    return (
      <div className="flex h-24 items-center justify-center">
        <Spinner size="sm" />
      </div>
    )
  }

  if (notifications.length === 0) {
    return <p className="px-4 py-6 text-center text-sm text-slate-400">No notifications</p>
  }

  return (
    <ul className="divide-y divide-slate-100">
      {notifications.map((n) => (
        <li
          key={n.id}
          className={`flex items-start gap-3 px-4 py-3 ${n.readAt ? 'opacity-60' : 'bg-blue-50/40'}`}
        >
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-slate-900 truncate">{n.subject}</p>
            <p className="mt-0.5 text-xs text-slate-500 line-clamp-2">{n.body}</p>
            <p className="mt-1 text-xs text-slate-400">
              {formatDistanceToNow(new Date(n.createdAt), { addSuffix: true })}
            </p>
          </div>
          {!n.readAt && (
            <button
              onClick={() => mutate(n.id)}
              className="mt-0.5 rounded p-1 text-slate-400 hover:bg-slate-200 hover:text-slate-600"
              aria-label="Mark as read"
            >
              <Check className="h-4 w-4" />
            </button>
          )}
        </li>
      ))}
    </ul>
  )
}
