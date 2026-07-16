import { Badge } from '@/shared/components/ui/Badge'
import type { DocumentStatus } from '@/shared/api/types'

const config: Record<DocumentStatus, { color: 'gray' | 'yellow' | 'green' | 'red'; label: string }> = {
  UPLOADED: { color: 'gray', label: 'Uploaded' },
  INDEXING: { color: 'yellow', label: 'Indexing…' },
  INDEXED: { color: 'green', label: 'Indexed' },
  FAILED: { color: 'red', label: 'Failed' },
}

export function StatusBadge({ status }: { status: DocumentStatus }) {
  const { color, label } = config[status] ?? { color: 'gray', label: status }
  return <Badge color={color}>{label}</Badge>
}
