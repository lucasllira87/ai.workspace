import { Card } from '@/shared/components/ui/Card'
import { ProgressBar } from '@/shared/components/ui/ProgressBar'
import type { BillingStats } from '@/shared/api/types'

function fmt(n: number) {
  return n >= 1_000_000
    ? `${(n / 1_000_000).toFixed(1)}M`
    : n >= 1_000
    ? `${(n / 1_000).toFixed(1)}K`
    : String(n)
}

export function UsageBar({ billing }: { billing: BillingStats }) {
  return (
    <Card>
      <div className="mb-4 flex items-center justify-between">
        <h3 className="font-semibold text-slate-900">Token Usage</h3>
        <span className="text-sm text-slate-500">{billing.planName}</span>
      </div>
      <ProgressBar
        value={billing.tokensUsedThisMonth}
        max={billing.maxTokensPerMonth}
        label={`${fmt(billing.tokensUsedThisMonth)} / ${fmt(billing.maxTokensPerMonth)} tokens`}
        showPercent
      />
      {billing.renewsAt && (
        <p className="mt-2 text-xs text-slate-400">
          Resets on {new Date(billing.renewsAt).toLocaleDateString()}
        </p>
      )}
    </Card>
  )
}
