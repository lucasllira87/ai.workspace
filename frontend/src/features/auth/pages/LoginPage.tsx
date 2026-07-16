import { Link, Navigate } from 'react-router-dom'
import { Brain } from 'lucide-react'
import { Card } from '@/shared/components/ui/Card'
import { useAuthStore } from '@/shared/store/authStore'
import { LoginForm } from '../components/LoginForm'

export function LoginPage() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated())
  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  return (
    <div className="flex min-h-full items-center justify-center p-4">
      <div className="w-full max-w-sm space-y-6">
        <div className="flex flex-col items-center gap-2">
          <Brain className="h-10 w-10 text-brand-600" />
          <h1 className="text-2xl font-bold text-slate-900">AI Workspace</h1>
          <p className="text-sm text-slate-500">Sign in to your account</p>
        </div>

        <Card>
          <LoginForm />
        </Card>

        <p className="text-center text-sm text-slate-600">
          Don&apos;t have an account?{' '}
          <Link to="/register" className="font-medium text-brand-600 hover:underline">
            Sign up
          </Link>
        </p>
      </div>
    </div>
  )
}
