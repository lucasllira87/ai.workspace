import { useState, useRef, useEffect, FormEvent } from 'react'
import { Send, StopCircle } from 'lucide-react'
import { Card } from '@/shared/components/ui/Card'
import { Button } from '@/shared/components/ui/Button'
import { Spinner } from '@/shared/components/ui/Spinner'
import { useAuthStore } from '@/shared/store/authStore'
import { sendChatMessage } from '../api'
import type { ChatMessage } from '@/shared/api/types'

export function ChatPanel({ documentId }: { documentId: string }) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [streaming, setStreaming] = useState(false)
  const abortRef = useRef<AbortController | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)
  const accessToken = useAuthStore((s) => s.accessToken)!

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    const question = input.trim()
    if (!question || streaming) return

    setInput('')
    setMessages((m) => [...m, { role: 'user', content: question }])
    setStreaming(true)

    const controller = new AbortController()
    abortRef.current = controller

    let assistantMsg = ''
    setMessages((m) => [...m, { role: 'assistant', content: '' }])

    try {
      await sendChatMessage(
        documentId,
        question,
        (chunk) => {
          assistantMsg += chunk
          setMessages((m) => {
            const copy = [...m]
            copy[copy.length - 1] = { role: 'assistant', content: assistantMsg }
            return copy
          })
        },
        controller.signal,
        accessToken,
      )
    } catch (err) {
      if ((err as Error).name !== 'AbortError') {
        setMessages((m) => {
          const copy = [...m]
          copy[copy.length - 1] = {
            role: 'assistant',
            content: 'An error occurred. Please try again.',
          }
          return copy
        })
      }
    } finally {
      setStreaming(false)
      abortRef.current = null
    }
  }

  const stop = () => {
    abortRef.current?.abort()
  }

  return (
    <Card padding="none" className="flex flex-col" style={{ height: '70vh' }}>
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 && (
          <p className="text-center text-sm text-slate-400 mt-8">
            Ask anything about this document
          </p>
        )}
        {messages.map((msg, i) => (
          <div
            key={i}
            className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
          >
            <div
              className={`max-w-[75%] rounded-xl px-4 py-2.5 text-sm ${
                msg.role === 'user'
                  ? 'bg-brand-600 text-white'
                  : 'bg-slate-100 text-slate-800'
              }`}
            >
              {msg.content || (streaming && <Spinner size="sm" />)}
            </div>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <form
        onSubmit={submit}
        className="flex gap-2 border-t border-slate-200 p-4"
      >
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask a question…"
          disabled={streaming}
          className="flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-100"
        />
        {streaming ? (
          <Button type="button" variant="secondary" size="md" onClick={stop}>
            <StopCircle className="h-4 w-4" />
          </Button>
        ) : (
          <Button type="submit" disabled={!input.trim()}>
            <Send className="h-4 w-4" />
          </Button>
        )}
      </form>
    </Card>
  )
}
