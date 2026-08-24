interface HeaderProps {
  titulo: string
  subtitulo: string
}

export default function Header({ titulo, subtitulo }: HeaderProps) {
  return (
    <header className="chat-header">
      <h1>{titulo}</h1>
      <p>{subtitulo}</p>
    </header>
  )
}
