import { useState, FormEvent } from 'react'

interface ChatInputProps {
  onEnviar: (pergunta: string) => void
  disabled: boolean
}

export default function ChatInput({ onEnviar, disabled }: ChatInputProps) {
  const [input, setInput] = useState('')

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const pergunta = input.trim()
    if (!pergunta || disabled) return
    setInput('')
    onEnviar(pergunta)
  }

  return (
    <form onSubmit={handleSubmit} className="chat-form">
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="Digite sua pergunta..."
        className="chat-input"
        disabled={disabled}
      />
      <button type="submit" className="chat-botao" disabled={disabled || !input.trim()}>
        Enviar
      </button>
    </form>
  )
}
