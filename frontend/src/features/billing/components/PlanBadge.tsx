import { Badge } from '@/shared/components/ui/Badge'

const planColors: Record<string, 'gray' | 'indigo' | 'green'> = {
  FREE: 'gray',
  TRIAL: 'indigo',
  PRO: 'green',
}

export function PlanBadge({ planName }: { planName: string }) {
  const color = planColors[planName.toUpperCase()] ?? 'gray'
  return <Badge color={color}>{planName}</Badge>
}
