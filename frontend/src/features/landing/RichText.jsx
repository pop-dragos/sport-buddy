// O frază în gri, cu bucățile importante în culoarea textului (ca pe paginile de produs Apple).
export default function RichText({ parts, className }) {
  return (
    <p className={className}>
      {parts.map((p, i) => (p.strong ? <span key={i} className="em">{p.t}</span> : <span key={i}>{p.t}</span>))}
    </p>
  )
}
