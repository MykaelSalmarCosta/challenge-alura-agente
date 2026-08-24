import { useState } from 'react'
import Header from './components/Header'
import MessageList, { Mensagem } from './components/MessageList'
import ChatInput from './components/ChatInput'

const API_URL = '/api/chat'

function App() {
  const [mensagens, setMensagens] = useState<Mensagem[]>([])
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function enviar(pergunta: string) {
    setErro(null)

    const novasMensagens: Mensagem[] = [...mensagens, { role: 'user', conteudo: pergunta }]
    setMensagens(novasMensagens)
    setCarregando(true)

    try {
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          pergunta,
          historico: mensagens,
        }),
      })

      if (!res.ok) {
        const data = await res.json().catch(() => null)
        throw new Error(data?.mensagem || `Erro ${res.status}`)
      }

      const data = await res.json()
      setMensagens([...novasMensagens, { role: 'assistant', conteudo: data.resposta }])
    } catch (err) {
      setErro(err instanceof Error ? err.message : 'Erro desconhecido')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="container">
      <Header titulo="Agente SPS" subtitulo="Assistente sobre documentos internos" />
      <MessageList mensagens={mensagens} carregando={carregando} />
      {erro && <p className="erro">{erro}</p>}
      <ChatInput onEnviar={enviar} disabled={carregando} />
    </div>
  )
}

export default App
