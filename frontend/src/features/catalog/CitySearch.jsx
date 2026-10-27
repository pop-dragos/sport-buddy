import { useState } from 'react'
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

  const suggestions = text === null ? [] : suggest(cities, text)

  function choose(c) {
    selectCity(c.id)
    setText(null)
  }

  function onKeyDown(e) {
    if (e.key === 'Escape') setText(null)
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
        value={text ?? city?.name ?? ''}
        onChange={(e) => {
          setText(e.target.value)
          setActive(0)
        }}
        onFocus={(e) => e.target.select()}
        onBlur={() => setText(null)}
        onKeyDown={onKeyDown}
        placeholder="Caută oraș"
      />

      {text !== null && text.trim() && (
        <ul className="city-list">
          {suggestions.length === 0 && <li className="empty">Niciun oraș găsit</li>}
          {suggestions.map((c, i) => (
            <li
              key={c.id}
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
