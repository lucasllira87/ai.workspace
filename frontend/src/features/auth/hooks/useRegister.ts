import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/shared/store/authStore'
import { useAutoToast } from '@/shared/components/ui/Toast'
import { register } from '../api'
import type { RegisterFormData } from '../types'

export function useRegister() {
  const { setTokens, setUser } = useAuthStore()
  const navigate = useNavigate()
  const toast = useAutoToast()

  return useMutation({
    mutationFn: ({ confirmPassword: _, ...data }: RegisterFormData) => register(data),
    onSuccess: ({ tokens, user }) => {
      setTokens(tokens.accessToken, tokens.refreshToken)
      setUser(user)
      navigate('/dashboard')
    },
    onError: (err: { response?: { status: number } }) => {
      if (err.response?.status === 409) {
        toast.error('Email already registered')
      } else {
        toast.error('Registration failed. Please try again.')
      }
    },
  })
}
