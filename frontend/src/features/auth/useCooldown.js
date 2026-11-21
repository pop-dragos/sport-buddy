import { useEffect, useState } from 'react'

// Câte secunde mai sunt până se poate apăsa iar (de exemplu „Retrimite emailul”). start() repornește numărătoarea.
export function useCooldown(seconds, startNow = false) {
  const [left, setLeft] = useState(startNow ? seconds : 0)

  useEffect(() => {
    if (left <= 0) return
    const t = setTimeout(() => setLeft((s) => s - 1), 1000)
    return () => clearTimeout(t)
  }, [left])

  return [left, () => setLeft(seconds)]
}
