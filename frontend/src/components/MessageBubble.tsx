interface MessageBubbleProps {
  role: 'user' | 'assistant'
  conteudo: string
}

export default function MessageBubble({ role, conteudo }: MessageBubbleProps) {
  const isUser = role === 'user'
  return (
    <div className={`bolha ${isUser ? 'bolha-usuario' : 'bolha-assistente'}`}>
      <span className="bolha-label">{isUser ? 'Você' : 'Agente'}</span>
      <p className="bolha-texto">{conteudo}</p>
    </div>
  )
}
