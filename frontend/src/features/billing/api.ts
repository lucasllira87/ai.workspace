import { api } from '@/shared/api/axios'
import { z } from 'zod'
import { SubscriptionDtoSchema, PlanSchema } from '@/shared/api/types'
import type { SubscriptionDto, Plan } from '@/shared/api/types'

export async function getSubscription(): Promise<SubscriptionDto> {
  const res = await api.get('/billing/subscription')
  return SubscriptionDtoSchema.parse(res.data)
}

export async function getPlans(): Promise<Plan[]> {
  const res = await api.get('/billing/plans')
  return z.array(PlanSchema).parse(res.data)
}

export async function cancelSubscription(): Promise<void> {
  await api.post('/billing/cancel')
}

export async function upgradeSubscription(planId: string): Promise<void> {
  await api.post('/billing/upgrade', { planId })
}
