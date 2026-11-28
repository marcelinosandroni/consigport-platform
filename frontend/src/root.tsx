import './styles.css'
import { resolveRoute } from './app/router.tsx'
import { Shell } from './app/shell.tsx'
import { DEFAULT_LOCALE, type SupportedLocale } from './shared/i18n/locales.ts'

export function Root(props: { url: URL; locale?: SupportedLocale }) {
  const locale = props.locale ?? DEFAULT_LOCALE

  return (
    <html lang={locale}>
      <head>
        <meta charSet="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>ConsigPort</title>
      </head>
      <body>
        <Shell pathname={props.url.pathname} locale={locale}>
          {resolveRoute(props.url.pathname, locale)}
        </Shell>
      </body>
    </html>
  )
}
