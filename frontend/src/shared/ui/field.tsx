import type { InputHTMLAttributes, ReactNode } from 'react'

/**
 * A labelled control with its hint and its error.
 *
 * The wrapper owns the accessibility wiring — the label, the description and
 * the invalid state — so every field of the platform announces the same way:
 * the control is named by its label, the hint and the error are linked by
 * `aria-describedby`, and a failed validation sets `aria-invalid`.
 */

export type FieldControlProps = {
  id: string
  'aria-describedby'?: string
  'aria-invalid'?: true
  required?: boolean
}

export type FieldProps = {
  /** The id of the control, shared by the label, the hint and the error. */
  id: string
  label: string
  /** The validation message; its presence marks the control as invalid. */
  error?: string
  /** A quiet instruction that is not an error. */
  hint?: string
  required?: boolean
  /** The control itself, wired with the ids the wrapper owns. */
  children: (props: FieldControlProps) => ReactNode
}

export function Field({ id, label, error, hint, required = false, children }: FieldProps) {
  const errorId = `${id}-error`
  const hintId = `${id}-hint`
  const describedBy = [hint ? hintId : null, error ? errorId : null].filter(Boolean).join(' ')

  return (
    <div className="flex flex-col gap-space-xs">
      <label
        htmlFor={id}
        className={[
          'font-mono text-label-mono uppercase tracking-[0.06em] text-text-secondary',
          // The asterisk is a decoration drawn by CSS: the name of the control
          // stays exactly the label, and `required` carries the obligation.
          required ? "after:content-['*']" : '',
        ]
          .filter(Boolean)
          .join(' ')}
      >
        {label}
      </label>

      {children({
        id,
        'aria-describedby': describedBy.length > 0 ? describedBy : undefined,
        'aria-invalid': error ? true : undefined,
        required,
      })}

      {hint ? (
        <p id={hintId} className="text-body-sm text-text-secondary">
          {hint}
        </p>
      ) : null}

      {error ? (
        <p id={errorId} role="alert" className="text-body-sm text-error">
          {error}
        </p>
      ) : null}
    </div>
  )
}

const controlClasses = [
  'min-h-11 w-full rounded border bg-surface-base px-space-sm',
  'text-body-md text-text-primary placeholder:text-text-muted',
  'border-border-subtle',
  // The hairline migrates to the accent colour on focus: no thick outline.
  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
  'focus-visible:border-primary',
  'disabled:cursor-not-allowed disabled:opacity-50',
  'aria-invalid:border-error',
].join(' ')

export type TextFieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> & {
  id: string
  label: string
  error?: string
  hint?: string
}

export function TextField({ id, label, error, hint, required = false, className, ...props }: TextFieldProps) {
  return (
    <Field id={id} label={label} error={error} hint={hint} required={required}>
      {(control) => (
        <input
          {...control}
          {...props}
          className={[controlClasses, className].filter(Boolean).join(' ')}
        />
      )}
    </Field>
  )
}
