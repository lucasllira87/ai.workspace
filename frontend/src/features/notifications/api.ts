import { api } from '@/shared/api/axios'
import { z } from 'zod'
import { NotificationSchema } from '@/shared/api/types'
import type { Notification } from '@/shared/api/types'

const ListSchema = z.array(NotificationSchema)

export async function getNotifications(): Promise<Notification[]> {
  const res = await api.get('/notifications')
  return ListSchema.parse(res.data)
}

export async function markAsRead(id: string): Promise<void> {
  await api.patch(`/notifications/${id}/read`)
}
