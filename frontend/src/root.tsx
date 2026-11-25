import './styles.css'
import { getServerCounter, updateServerCounter } from './action.tsx'
import { ClientCounter } from './client.tsx'

export function Root(props: { url: URL }) {
  return (
    <html lang="pt-BR">
      <head>
        <meta charSet="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>ConsigPort</title>
      </head>
      <body>
        <App {...props} />
      </body>
    </html>
  )
}

function App(props: { url: URL }) {
  return (
    <main className="p-8">
      <h1 className="text-headline-lg">ConsigPort</h1>
      <p className="text-body-md text-text-secondary">
        Base RSC pronta para o shell Matrix da Task 0.9.
      </p>
      <ClientCounter />
      <form action={updateServerCounter.bind(null, 1)}>
        <button>Server Counter: {getServerCounter()}</button>
      </form>
      <p className="text-label-mono">Request URL: {props.url?.href}</p>
    </main>
  )
}
