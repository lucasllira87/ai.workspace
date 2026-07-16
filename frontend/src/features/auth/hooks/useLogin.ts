import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/shared/store/authStore'
import { useAutoToast } from '@/shared/components/ui/Toast'
import { login } from '../api'
import type { LoginFormData } from '../types'

export function useLogin() {
  const { setTokens, setUser } = useAuthStore()
  const navigate = useNavigate()
  const toast = useAutoToast()

  return useMutation({
    mutationFn: (data: LoginFormData) => login(data),
    onSuccess: ({ tokens, user }) => {
      setTokens(tokens.accessToken, tokens.refreshToken)
      setUser(user)
      navigate('/dashboard')
    },
    onError: () => {
      toast.error('Invalid email or password')
    },
  })
}
