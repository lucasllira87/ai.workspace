import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Card } from '@/shared/components/ui/Card'
import { Button } from '@/shared/components/ui/Button'
import { Spinner } from '@/shared/components/ui/Spinner'
import { useAutoToast } from '@/shared/components/ui/Toast'
import { PlanBadge } from '../components/PlanBadge'
import { UsageProgressBar } from '../components/UsageProgressBar'
import { getSubscription, getPlans, cancelSubscription, upgradeSubscription } from '../api'

export function BillingPage() {
  const queryClient = useQueryClient()
  const toast = useAutoToast()

  const { data: sub, isLoading: loadingSub } = useQuery({
    queryKey: ['subscription'],
    queryFn: getSubscription,
  })

  const { data: plans = [], isLoading: loadingPlans } = useQuery({
    queryKey: ['plans'],
    queryFn: getPlans,
  })

  const { mutate: cancel, isPending: canceling } = useMutation({
    mutationFn: cancelSubscription,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscription'] })
      toast.success('Subscription canceled')
    },
    onError: () => toast.error('Failed to cancel subscription'),
  })

  const { mutate: upgrade, isPending: upgrading } = useMutation({
    mutationFn: upgradeSubscription,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscription'] })
      toast.success('Plan upgraded successfully')
    },
    onError: () => toast.error('Failed to upgrade plan'),
  })

  if (loadingSub) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Billing</h1>

      {sub && (
        <Card>
          <div className="flex items-start justify-between">
            <div>
              <h2 className="font-semibold text-slate-900">Current Plan</h2>
              <div className="mt-1 flex items-center gap-2">
                <PlanBadge planName={sub.planName} />
                <span className="text-sm text-slate-500 capitalize">
                  {sub.status.toLowerCase().replace(/_/g, ' ')}
                </span>
              </div>
              <p className="mt-2 text-xs text-slate-400">
                Period: {new Date(sub.currentPeriodStart).toLocaleDateString()} –{' '}
                {new Date(sub.currentPeriodEnd).toLocaleDateString()}
              </p>
            </div>
            {sub.status === 'ACTIVE' && (
              <Button
                variant="danger"
                size="sm"
                loading={canceling}
                onClick={() => cancel()}
              >
                Cancel
              </Button>
            )}
          </div>
        </Card>
      )}

      <div>
        <h2 className="mb-3 font-semibold text-slate-900">Available Plans</h2>
        {loadingPlans ? (
          <Spinner />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {plans.map((plan) => (
              <Card key={plan.id} className="flex flex-col gap-4">
                <div>
                  <h3 className="font-bold text-slate-900">{plan.name}</h3>
                  <p className="mt-1 text-2xl font-bold text-brand-600">
                    {plan.price.currency} {plan.price.amount.toFixed(2)}
                    <span className="text-sm font-normal text-slate-400">
                      /{plan.billingCycle.toLowerCase()}
                    </span>
                  </p>
                </div>

                <ul className="flex-1 space-y-1 text-sm text-slate-600">
                  {plan.features.map((f) => (
                    <li key={f} className="flex items-center gap-2">
                      <span className="text-green-500">✓</span> {f}
                    </li>
                  ))}
                </ul>

                <UsageProgressBar
                  label={`${(plan.quota.maxTokensPerMonth / 1000).toFixed(0)}K tokens/month`}
                  used={0}
                  max={plan.quota.maxTokensPerMonth}
                />

                <Button
                  variant={sub?.planId === plan.id ? 'secondary' : 'primary'}
                  disabled={sub?.planId === plan.id}
                  loading={upgrading}
                  onClick={() => upgrade(plan.id)}
                  className="w-full"
                >
                  {sub?.planId === plan.id ? 'Current Plan' : 'Upgrade'}
                </Button>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
