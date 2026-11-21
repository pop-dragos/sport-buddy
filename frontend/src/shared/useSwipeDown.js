import { useRef, useState } from 'react'

const CLOSE_AFTER_PX = 80

// Pe telefon, foile de jos se închid trăgând în jos de bara de prindere (ca în iOS).
// handleProps se pun pe bara de prindere, sheetStyle pe foaie, ca să urmărească degetul.
export default function useSwipeDown(onClose) {
  const startY = useRef(null)
  // distanța e ținută și în ref: la o tragere rapidă, starea poate să nu fie încă actualizată la ridicarea degetului
  const distance = useRef(0)
  const [offset, setOffset] = useState(0)

  const handleProps = {
    onTouchStart: (e) => {
      startY.current = e.touches[0].clientY
      distance.current = 0
    },
    onTouchMove: (e) => {
      if (startY.current === null) return
      distance.current = Math.max(0, e.touches[0].clientY - startY.current)
      setOffset(distance.current)
    },
    onTouchEnd: () => {
      startY.current = null
      if (distance.current > CLOSE_AFTER_PX) onClose()
      distance.current = 0
      setOffset(0)
    }
  }

  const sheetStyle = offset > 0 ? { transform: `translateY(${offset}px)`, transition: 'none' } : undefined

  return { handleProps, sheetStyle }
}
