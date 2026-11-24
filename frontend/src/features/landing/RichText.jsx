import { useT } from '../../shared/i18n/useI18n'

// O frază în gri, cu bucățile importante în culoarea textului (ca pe paginile de produs Apple).
export default function RichText({ parts, className }) {
  const t = useT()
  return (
    <p className={className}>
      {parts.map((p, i) => (p.strong ? <span key={i} className="em">{t(p.t)}</span> : <span key={i}>{t(p.t)}</span>))}
    </p>
  )
}
