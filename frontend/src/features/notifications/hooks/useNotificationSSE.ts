import { useEffect, useCallback } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/shared/store/authStore'
import { NotificationSchema } from '@/shared/api/types'

export function useNotificationSSE() {
  const accessToken = useAuthStore((s) => s.accessToken)
  const queryClient = useQueryClient()

  const connect = useCallback(() => {
    if (!accessToken) return

    let active = true
    let controller: AbortController | null = new AbortController()

    const startStream = async () => {
      try {
        const response = await fetch('/api/notifications/stream', {
          headers: { Authorization: `Bearer ${accessToken}` },
          signal: controller?.signal,
        })

        if (!response.body) return

        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''

        while (active) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() ?? ''

          for (const line of lines) {
            if (!line.startsWith('data:')) continue
            const raw = line.slice(5).trim()
            if (!raw || raw === 'ping') continue
            try {
              const parsed = NotificationSchema.safeParse(JSON.parse(raw))
              if (parsed.success) {
                queryClient.invalidateQueries({ queryKey: ['notifications'] })
              }
            } catch {
              // malformed SSE payload — skip
            }
          }
        }
      } catch (err) {
        if ((err as Error).name === 'AbortError') return
        // reconnect after 5s on transient errors
        if (active) setTimeout(startStream, 5_000)
      }
    }

    startStream()

    return () => {
      active = false
      controller?.abort()
      controller = null
    }
  }, [accessToken, queryClient])

  useEffect(() => {
    const cleanup = connect()
    return cleanup
  }, [connect])
}
