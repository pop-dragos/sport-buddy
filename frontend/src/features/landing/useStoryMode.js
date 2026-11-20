import { useEffect, useState } from 'react'

// Povestea legată de scroll are nevoie de spațiu: o fereastră mare și titlul deasupra.
const WIDE = '(min-width: 900px) and (min-height: 640px)'
const REDUCED = '(prefers-reduced-motion: reduce)'

function useMedia(query) {
  const [matches, setMatches] = useState(() => typeof window !== 'undefined' && window.matchMedia(query).matches)

  // ascultăm schimbările (fereastră redimensionată, „reduce motion" pornit din sistem) fără reîncărcare
  useEffect(() => {
    const list = window.matchMedia(query)
    const onChange = () => setMatches(list.matches)
    list.addEventListener('change', onChange)
    return () => list.removeEventListener('change', onChange)
  }, [query])

  return matches
}

// mode 'scroll': fereastra fixă care se schimbă pe măsură ce derulezi.
// mode 'static': cele trei etape una sub alta (ecrane mici sau „reduce motion" pornit în sistem).
// reduced: fără nicio animație, nici la apariția etapelor.
export function useStoryMode() {
  const wide = useMedia(WIDE)
  const reduced = useMedia(REDUCED)
  return { mode: wide && !reduced ? 'scroll' : 'static', reduced }
}
