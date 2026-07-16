import { useNavigate } from 'react-router-dom'
import { Brain } from 'lucide-react'
import { Button } from '@/shared/components/ui/Button'

export function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <div className="flex min-h-full flex-col items-center justify-center gap-6 p-8">
      <Brain className="h-16 w-16 text-brand-300" />
      <div className="text-center">
        <h1 className="text-6xl font-bold text-slate-200">404</h1>
        <p className="mt-2 text-lg font-medium text-slate-600">Page not found</p>
        <p className="mt-1 text-sm text-slate-400">
          The page you&apos;re looking for doesn&apos;t exist.
        </p>
      </div>
      <Button onClick={() => navigate(-1)}>Go back</Button>
    </div>
  )
}
