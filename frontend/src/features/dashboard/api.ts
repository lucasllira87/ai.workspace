import { api } from '@/shared/api/axios'
import { DashboardStatsSchema, DashboardStats } from '@/shared/api/types'

export async function getDashboardStats(): Promise<DashboardStats> {
  const res = await api.get('/dashboard')
  return DashboardStatsSchema.parse(res.data)
}
