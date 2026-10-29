import { useEffect, useRef, useState } from 'react'
import { useCity } from './useCity'
import './catalog.css'

const MAX_SUGGESTIONS = 8

// „Brașov" -> „brasov", ca orașul să fie găsit și fără diacritice
function simplify(s) {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim()
}

// Întâi orașele care încep cu textul, apoi cele care doar îl conțin
function suggest(cities, text) {
  const q = simplify(text)
  if (!q) return []
  const starts = cities.filter((c) => simplify(c.name).startsWith(q))
  const contains = cities.filter((c) => !simplify(c.name).startsWith(q) && simplify(c.name).includes(q))
  return [...starts, ...contains].slice(0, MAX_SUGGESTIONS)
}

export default function CitySearch() {
  const { cities, city, selectCity } = useCity()
  // textul tastat; null = nu scrie nimic acum, se afișează orașul ales
  const [text, setText] = useState(null)
  const [active, setActive] = useState(0)
  // true = lista cu toate orașele, deschisă din săgeată
  const [showAll, setShowAll] = useState(false)
  const inputRef = useRef(null)
  const activeRef = useRef(null)

  const typing = text !== null && text.trim() !== ''
  const suggestions = typing ? suggest(cities, text) : showAll ? cities : []
  const open = typing || showAll

  // cu săgețile de la tastatură, lista derulează după orașul evidențiat
  useEffect(() => {
    activeRef.current?.scrollIntoView({ block: 'nearest' })
  }, [active])

  function choose(c) {
    selectCity(c.id)
    setText(null)
    setShowAll(false)
  }

  function close() {
    setText(null)
    setShowAll(false)
  }

  function toggleAll() {
    if (showAll) {
      close()
      return
    }
    const current = cities.findIndex((c) => c.id === city?.id)
    setActive(Math.max(0, current))
    setShowAll(true)
    inputRef.current.focus()
  }

  function onKeyDown(e) {
    if (e.key === 'Escape') close()
    if (suggestions.length === 0) return
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      setActive((active + 1) % suggestions.length)
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      setActive((active - 1 + suggestions.length) % suggestions.length)
    } else if (e.key === 'Enter') {
      choose(suggestions[active])
    }
  }

  return (
    <div className="city-search">
      <input
        ref={inputRef}
        value={text ?? city?.name ?? ''}
        onChange={(e) => {
          setText(e.target.value)
          setActive(0)
        }}
        onFocus={(e) => e.target.select()}
        onBlur={close}
        onKeyDown={onKeyDown}
        placeholder="Caută oraș"
      />
      <button
        type="button"
        className="city-toggle"
        title="Toate orașele"
        onMouseDown={(e) => {
          // fără asta, click-ul ar scoate focusul din câmp și lista s-ar închide
          e.preventDefault()
          toggleAll()
        }}
      >
        ▾
      </button>

      {open && (
        <ul className="city-list">
          {suggestions.length === 0 && <li className="empty">Niciun oraș găsit</li>}
          {suggestions.map((c, i) => (
            <li
              key={c.id}
              ref={i === active ? activeRef : null}
              className={i === active ? 'on' : ''}
              onMouseDown={(e) => {
                e.preventDefault()
                choose(c)
              }}
              onMouseEnter={() => setActive(i)}
            >
              {c.name}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
