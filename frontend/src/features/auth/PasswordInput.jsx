import { useState } from 'react'

export default function PasswordInput({ value, onChange, placeholder, autoComplete }) {
  const [shown, setShown] = useState(false)

  return (
    <div className="pw">
      <input
        type={shown ? 'text' : 'password'}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        autoComplete={autoComplete}
      />
      <button type="button" className="eye" title="Arată parola" onClick={() => setShown(!shown)}>
        <svg className="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
      </button>
    </div>
  )
}
