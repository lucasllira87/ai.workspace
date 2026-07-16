import { ProgressBar } from '@/shared/components/ui/ProgressBar'

interface UsageProgressBarProps {
  label: string
  used: number
  max: number
}

export function UsageProgressBar({ label, used, max }: UsageProgressBarProps) {
  return <ProgressBar label={label} value={used} max={max} showPercent />
}
