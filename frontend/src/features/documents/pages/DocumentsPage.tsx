import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Spinner } from '@/shared/components/ui/Spinner'
import { useAutoToast } from '@/shared/components/ui/Toast'
import { getDocuments, deleteDocument } from '../api'
import { DocumentCard } from '../components/DocumentCard'
import { UploadDropzone } from '../components/UploadDropzone'

export function DocumentsPage() {
  const queryClient = useQueryClient()
  const toast = useAutoToast()

  const { data: docs = [], isLoading } = useQuery({
    queryKey: ['documents'],
    queryFn: getDocuments,
    refetchInterval: 10_000,
  })

  const { mutate: remove } = useMutation({
    mutationFn: deleteDocument,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] })
      toast.success('Document deleted')
    },
    onError: () => toast.error('Failed to delete document'),
  })

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Documents</h1>

      <UploadDropzone />

      {isLoading ? (
        <div className="flex h-32 items-center justify-center">
          <Spinner size="lg" />
        </div>
      ) : docs.length === 0 ? (
        <p className="py-10 text-center text-slate-400">No documents yet. Upload one above.</p>
      ) : (
        <div className="space-y-3">
          {docs.map((doc) => (
            <DocumentCard key={doc.id} doc={doc} onDelete={remove} />
          ))}
        </div>
      )}
    </div>
  )
}
