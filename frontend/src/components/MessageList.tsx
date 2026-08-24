import { useRef, useEffect } from 'react'
import MessageBubble from './MessageBubble'

export interface Mensagem {
  role: 'user' | 'assistant'
  conteudo: string
}

interface MessageListProps {
  mensagens: Mensagem[]
  carregando: boolean
}

export default function MessageList({ mensagens, carregando }: MessageListProps) {
  const listaRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    listaRef.current?.scrollTo({ top: listaRef.current.scrollHeight, behavior: 'smooth' })
  }, [mensagens])

  return (
    <div ref={listaRef} className="mensagens">
      {mensagens.length === 0 && !carregando && (
        <p className="vazio">Faça uma pergunta sobre os documentos carregados.</p>
      )}

      {mensagens.map((msg, i) => (
        <MessageBubble key={i} role={msg.role} conteudo={msg.conteudo} />
      ))}

      {carregando && (
        <div className="bolha bolha-assistente">
          <span className="bolha-label">Agente</span>
          <p className="bolha-texto carregando-text">
            <span className="dot-pulse" />
            Pensando...
          </p>
        </div>
      )}
    </div>
  )
}
