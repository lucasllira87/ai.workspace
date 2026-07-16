import { FileText, Trash2, MessageSquare } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Card } from '@/shared/components/ui/Card'
import { Button } from '@/shared/components/ui/Button'
import { StatusBadge } from './StatusBadge'
import type { DocumentDto } from '@/shared/api/types'

function fmtBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1_048_576) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1_048_576).toFixed(1)} MB`
}

interface DocumentCardProps {
  doc: DocumentDto
  onDelete: (id: string) => void
}

export function DocumentCard({ doc, onDelete }: DocumentCardProps) {
  const navigate = useNavigate()

  return (
    <Card padding="sm" className="flex items-center gap-4">
      <FileText className="h-8 w-8 flex-shrink-0 text-slate-400" />
      <div className="min-w-0 flex-1">
        <p className="truncate font-medium text-slate-900">{doc.fileName}</p>
        <p className="text-xs text-slate-400">{fmtBytes(doc.sizeBytes)}</p>
      </div>
      <StatusBadge status={doc.status} />
      <div className="flex gap-2">
        <Button
          variant="ghost"
          size="sm"
          disabled={doc.status !== 'INDEXED'}
          onClick={() => navigate(`/documents/${doc.id}/chat`)}
        >
          <MessageSquare className="h-4 w-4" />
        </Button>
        <Button variant="ghost" size="sm" onClick={() => onDelete(doc.id)}>
          <Trash2 className="h-4 w-4 text-red-500" />
        </Button>
      </div>
    </Card>
  )
}
