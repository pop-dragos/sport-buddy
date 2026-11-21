import { createPortal } from 'react-dom'
import useSwipeDown from './useSwipeDown'

// Pe desktop: fereastră în mijlocul ecranului. Pe telefon: foaie care urcă de jos,
// cu bară de prindere; se închide trăgând-o în jos, cu click în afară sau din butoanele ei.
// Se afișează direct în <body> (portal): un părinte cu blur (header, card) ar „închide” altfel
// fereastra fixă în interiorul lui, iar pagina ar acoperi-o.
export default function Modal({ title, onClose, children }) {
  const { handleProps, sheetStyle } = useSwipeDown(onClose)

  return createPortal(
    <div className="ov" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="mb" role="dialog" aria-modal="true" style={sheetStyle}>
        <div className="mb-grab" aria-hidden="true" {...handleProps}><span /></div>
        <h3>{title}</h3>
        {children}
      </div>
    </div>,
    document.body
  )
}
