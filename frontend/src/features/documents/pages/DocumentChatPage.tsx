import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { Spinner } from '@/shared/components/ui/Spinner'
import { Button } from '@/shared/components/ui/Button'
import { getDocuments } from '../api'
import { ChatPanel } from '../components/ChatPanel'
import { StatusBadge } from '../components/StatusBadge'

export function DocumentChatPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: docs = [], isLoading } = useQuery({
    queryKey: ['documents'],
    queryFn: getDocuments,
  })

  const doc = docs.find((d) => d.id === id)

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    )
  }

  if (!doc) {
    return (
      <div className="flex h-64 flex-col items-center justify-center gap-4">
        <p className="text-slate-500">Document not found.</p>
        <Button variant="secondary" onClick={() => navigate('/documents')}>
          Back to Documents
        </Button>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <Link to="/documents" className="text-slate-400 hover:text-slate-600">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <h1 className="flex-1 truncate text-xl font-bold text-slate-900">{doc.fileName}</h1>
        <StatusBadge status={doc.status} />
      </div>

      {doc.status !== 'INDEXED' ? (
        <div className="flex h-64 items-center justify-center rounded-xl border-2 border-dashed border-slate-200 text-slate-400">
          Document must be indexed before chatting. Status: {doc.status}
        </div>
      ) : (
        <ChatPanel documentId={doc.id} />
      )}
    </div>
  )
}
