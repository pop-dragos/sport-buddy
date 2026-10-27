export default function Modal({ title, onClose, children }) {
  return (
    <div className="ov" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="mb" role="dialog" aria-modal="true">
        <h3>{title}</h3>
        {children}
      </div>
    </div>
  )
}
