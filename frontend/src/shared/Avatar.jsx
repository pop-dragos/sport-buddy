// Poza de profil sau, dacă lipsește, inițiala numelui pe fundalul în degrade.
export default function Avatar({ name, url, size = 30 }) {
  // la mărimea mică din meniu inițiala rămâne la fontul normal, ca în prototip
  const style = { width: size, height: size, ...(size > 30 && { fontSize: Math.round(size * 0.4) }) }

  if (url) {
    return <img className="av" src={url} alt={name} style={{ ...style, objectFit: 'cover' }} />
  }
  return <span className="av" style={style}>{name.charAt(0).toUpperCase()}</span>
}
