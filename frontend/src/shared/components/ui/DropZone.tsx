import { useCallback, useState, DragEvent, ChangeEvent } from 'react'
import { UploadCloud } from 'lucide-react'
import { clsx } from 'clsx'

interface DropZoneProps {
  onFiles: (files: File[]) => void
  accept?: string
  multiple?: boolean
  disabled?: boolean
}

export function DropZone({ onFiles, accept, multiple = false, disabled }: DropZoneProps) {
  const [dragging, setDragging] = useState(false)

  const handleDrop = useCallback(
    (e: DragEvent<HTMLDivElement>) => {
      e.preventDefault()
      setDragging(false)
      if (disabled) return
      const files = Array.from(e.dataTransfer.files)
      if (files.length) onFiles(files)
    },
    [disabled, onFiles],
  )

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files ?? [])
    if (files.length) onFiles(files)
    e.target.value = ''
  }

  return (
    <div
      onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
      onDragLeave={() => setDragging(false)}
      onDrop={handleDrop}
      className={clsx(
        'flex flex-col items-center justify-center gap-3 rounded-xl border-2 border-dashed p-10',
        'cursor-pointer transition-colors',
        dragging ? 'border-brand-500 bg-brand-50' : 'border-slate-300 hover:border-brand-400 hover:bg-slate-50',
        disabled && 'cursor-not-allowed opacity-50',
      )}
    >
      <UploadCloud className="h-10 w-10 text-slate-400" />
      <div className="text-center">
        <p className="text-sm font-medium text-slate-700">
          Drag & drop files here, or{' '}
          <label className="cursor-pointer text-brand-600 hover:underline">
            browse
            <input
              type="file"
              className="sr-only"
              accept={accept}
              multiple={multiple}
              disabled={disabled}
              onChange={handleChange}
            />
          </label>
        </p>
        {accept && (
          <p className="mt-1 text-xs text-slate-400">Accepted: {accept}</p>
        )}
      </div>
    </div>
  )
}
