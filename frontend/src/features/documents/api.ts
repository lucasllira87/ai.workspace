import { api } from '@/shared/api/axios'
import { z } from 'zod'
import { DocumentDtoSchema } from '@/shared/api/types'
import type { DocumentDto } from '@/shared/api/types'

const ListSchema = z.array(DocumentDtoSchema)

export async function getDocuments(): Promise<DocumentDto[]> {
  const res = await api.get('/documents')
  return ListSchema.parse(res.data)
}

export async function uploadDocument(file: File): Promise<DocumentDto> {
  const form = new FormData()
  form.append('file', file)
  const res = await api.post('/documents', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return DocumentDtoSchema.parse(res.data)
}

export async function deleteDocument(id: string): Promise<void> {
  await api.delete(`/documents/${id}`)
}

export async function sendChatMessage(
  documentId: string,
  question: string,
  onChunk: (chunk: string) => void,
  signal: AbortSignal,
  accessToken: string,
): Promise<void> {
  const response = await fetch(`/api/documents/${documentId}/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ question }),
    signal,
  })

  if (!response.ok) throw new Error(`Chat error ${response.status}`)
  if (!response.body) return

  const reader = response.body.getReader()
  const decoder = new TextDecoder()

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    onChunk(decoder.decode(value, { stream: true }))
  }
}
