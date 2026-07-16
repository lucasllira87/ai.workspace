import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { DropZone } from '@/shared/components/ui/DropZone'
import { ProgressBar } from '@/shared/components/ui/ProgressBar'
import { useAutoToast } from '@/shared/components/ui/Toast'
import { uploadDocument } from '../api'

export function UploadDropzone() {
  const [uploading, setUploading] = useState(false)
  const queryClient = useQueryClient()
  const toast = useAutoToast()

  const { mutate } = useMutation({
    mutationFn: (file: File) => uploadDocument(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] })
      toast.success('Document uploaded and queued for indexing')
      setUploading(false)
    },
    onError: () => {
      toast.error('Upload failed. Please try again.')
      setUploading(false)
    },
  })

  const handleFiles = (files: File[]) => {
    const file = files[0]
    if (!file) return
    setUploading(true)
    mutate(file)
  }

  return (
    <div className="space-y-3">
      <DropZone
        onFiles={handleFiles}
        accept=".pdf,.txt,.md,.docx"
        disabled={uploading}
      />
      {uploading && <ProgressBar value={100} max={100} label="Uploading…" />}
    </div>
  )
}
