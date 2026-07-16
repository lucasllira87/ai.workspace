import { useQuery } from '@tanstack/react-query'
import { FileText, BookOpen, CreditCard, Activity } from 'lucide-react'
import { Spinner } from '@/shared/components/ui/Spinner'
import { getDashboardStats } from '../api'
import { StatCard } from '../components/StatCard'
import { UsageBar } from '../components/UsageBar'
import { RecentActivity } from '../components/RecentActivity'

export function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboardStats,
    refetchInterval: 60_000,
  })

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div className="flex h-64 items-center justify-center text-red-500">
        Failed to load dashboard. Please refresh.
      </div>
    )
  }

  const { documents, learning, billing, recentActivity } = data

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Total Documents"
          value={documents.totalDocuments}
          icon={FileText}
          sub={`${documents.indexedDocuments} indexed`}
        />
        <StatCard
          label="Active Enrollments"
          value={learning.activeEnrollments}
          icon={BookOpen}
          sub={`${learning.completedCourses} completed`}
          iconColor="text-green-600"
        />
        <StatCard
          label="Plan"
          value={billing.planName}
          icon={CreditCard}
          sub={billing.subscriptionStatus}
          iconColor="text-purple-600"
        />
        <StatCard
          label="Lessons Completed"
          value={learning.totalLessonsCompleted}
          icon={Activity}
          iconColor="text-orange-600"
        />
      </div>

      <UsageBar billing={billing} />

      <RecentActivity activities={recentActivity} />
    </div>
  )
}
