/**
 * The navigation of the single-page application.
 *
 * The interface changes the address without leaving the page: the browser
 * history is the router, and the server answers the new screen with the RSC
 * payload. The entry of the browser listens to the history and asks the server
 * for the screen the address now names.
 *
 * The change of address happens **outside the work of React**. A navigation
 * scheduled from inside an effect is entangled with the render React is
 * finishing, and React drops the re-render that follows it: the address points
 * to the new screen while the old one stays on the display. The microtask
 * leaves that work behind, so the screen always follows the address.
 */

/**
 * Carries the reader to another screen of the application.
 *
 * @param target the path with the query, relative to the origin
 */
export const navigate = (target: string): void => {
  queueMicrotask(() => {
    window.history.pushState(null, '', target)
  })
}
