import './styles.css'
import { resolveRoute } from './app/router.tsx'
import { Shell } from './app/shell.tsx'

export function Root(props: { url: URL }) {
  return (
    <html lang="pt-BR">
      <head>
        <meta charSet="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>ConsigPort</title>
      </head>
      <body>
        <Shell pathname={props.url.pathname}>{resolveRoute(props.url.pathname)}</Shell>
      </body>
    </html>
  )
}
