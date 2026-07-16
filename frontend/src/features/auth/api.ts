import { api } from '@/shared/api/axios'
import { AuthResponseSchema, AuthResponse } from '@/shared/api/types'
import type { LoginFormData, RegisterFormData } from './types'

export async function login(data: LoginFormData): Promise<AuthResponse> {
  const res = await api.post('/auth/login', data)
  return AuthResponseSchema.parse(res.data)
}

export async function register(
  data: Omit<RegisterFormData, 'confirmPassword'>,
): Promise<AuthResponse> {
  const res = await api.post('/auth/register', data)
  return AuthResponseSchema.parse(res.data)
}
