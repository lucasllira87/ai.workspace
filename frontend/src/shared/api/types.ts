import { z } from 'zod'

// --- Common ---
export const ApiErrorSchema = z.object({
  message: z.string(),
  code: z.string().optional(),
  details: z.record(z.string()).optional(),
})
export type ApiError = z.infer<typeof ApiErrorSchema>

export const PageSchema = <T extends z.ZodTypeAny>(itemSchema: T) =>
  z.object({
    content: z.array(itemSchema),
    totalElements: z.number(),
    totalPages: z.number(),
    number: z.number(),
    size: z.number(),
  })

// --- Auth ---
export const TokensSchema = z.object({
  accessToken: z.string(),
  refreshToken: z.string(),
})
export type Tokens = z.infer<typeof TokensSchema>

export const UserDtoSchema = z.object({
  id: z.string().uuid(),
  email: z.string().email(),
  fullName: z.string(),
  roles: z.array(z.string()),
  status: z.string(),
})
export type UserDto = z.infer<typeof UserDtoSchema>

export const AuthResponseSchema = z.object({
  user: UserDtoSchema,
  tokens: TokensSchema,
})
export type AuthResponse = z.infer<typeof AuthResponseSchema>

// --- Dashboard ---
export const DocumentStatsSchema = z.object({
  totalDocuments: z.number(),
  indexedDocuments: z.number(),
  totalStorageBytes: z.number(),
})

export const LearningStatsSchema = z.object({
  activeEnrollments: z.number(),
  completedCourses: z.number(),
  totalLessonsCompleted: z.number(),
})

export const BillingStatsSchema = z.object({
  planName: z.string(),
  tokensUsedThisMonth: z.number(),
  maxTokensPerMonth: z.number(),
  subscriptionStatus: z.string(),
  renewsAt: z.string().nullable(),
})

export const RecentActivitySchema = z.object({
  id: z.string(),
  module: z.string(),
  eventType: z.string(),
  occurredAt: z.string(),
  metadata: z.record(z.unknown()).optional(),
})

export type DocumentStats = z.infer<typeof DocumentStatsSchema>
export type LearningStats = z.infer<typeof LearningStatsSchema>
export type BillingStats = z.infer<typeof BillingStatsSchema>

export const DashboardStatsSchema = z.object({
  documents: DocumentStatsSchema,
  learning: LearningStatsSchema,
  billing: BillingStatsSchema,
  recentActivity: z.array(RecentActivitySchema),
})
export type DashboardStats = z.infer<typeof DashboardStatsSchema>
export type RecentActivity = z.infer<typeof RecentActivitySchema>

// --- Documents ---
export const DocumentStatusSchema = z.enum(['UPLOADED', 'INDEXING', 'INDEXED', 'FAILED'])
export type DocumentStatus = z.infer<typeof DocumentStatusSchema>

export const DocumentDtoSchema = z.object({
  id: z.string().uuid(),
  fileName: z.string(),
  contentType: z.string(),
  sizeBytes: z.number(),
  status: DocumentStatusSchema,
  uploadedAt: z.string(),
  indexedAt: z.string().nullable(),
})
export type DocumentDto = z.infer<typeof DocumentDtoSchema>

export const ChatMessageSchema = z.object({
  role: z.enum(['user', 'assistant']),
  content: z.string(),
})
export type ChatMessage = z.infer<typeof ChatMessageSchema>

// --- Billing ---
export const PlanSchema = z.object({
  id: z.string().uuid(),
  name: z.string(),
  price: z.object({ amount: z.number(), currency: z.string() }),
  billingCycle: z.enum(['MONTHLY', 'ANNUAL']),
  quota: z.object({
    maxTokensPerMonth: z.number(),
    maxDocuments: z.number(),
    maxStorageBytes: z.number(),
    maxEnrollments: z.number(),
  }),
  features: z.array(z.string()),
})
export type Plan = z.infer<typeof PlanSchema>

export const SubscriptionDtoSchema = z.object({
  id: z.string().uuid(),
  status: z.string(),
  planId: z.string().uuid(),
  planName: z.string(),
  currentPeriodStart: z.string(),
  currentPeriodEnd: z.string(),
})
export type SubscriptionDto = z.infer<typeof SubscriptionDtoSchema>

// --- Notifications ---
export const NotificationSchema = z.object({
  id: z.string().uuid(),
  type: z.enum(['EMAIL', 'IN_APP']),
  subject: z.string(),
  body: z.string(),
  readAt: z.string().nullable(),
  createdAt: z.string(),
})
export type Notification = z.infer<typeof NotificationSchema>
