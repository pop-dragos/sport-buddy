// Iconițele sporturilor (liniare, 18 × 18, stroke 1,8) și culoarea fiecăruia, după `code` din backend.
// Folosite în bara laterală și pe prima pagină; culoarea implicită e cea a sportului.
const SPORT_COLORS = {
  football: '#16a34a',
  basketball: '#ea580c',
  tennis: '#ca8a04',
  padel: '#0891b2',
  volleyball: '#db2777',
  handball: '#7c3aed',
  badminton: '#0d9488',
  'table-tennis': '#dc2626',
  squash: '#2563eb',
  running: '#65a30d',
  cycling: '#4f46e5',
  swimming: '#0284c7'
}

export default function SportIcon({ type, color = SPORT_COLORS[type] }) {
  return (
    <svg className="ic" style={{ color }} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      {type === 'football' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M12 8l3.5 2.5-1.3 4h-4.4l-1.3-4z" />
        </>
      )}
      {type === 'basketball' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="12" y1="3" x2="12" y2="21" />
          <path d="M5.5 5.5c4 3 4 10 0 13" />
          <path d="M18.5 5.5c-4 3-4 10 0 13" />
        </>
      )}
      {type === 'tennis' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M5 5c4 3 4 11 0 14" />
          <path d="M19 5c-4 3-4 11 0 14" />
        </>
      )}
      {type === 'padel' && (
        <>
          <circle cx="10" cy="10" r="6" />
          <line x1="14" y1="14" x2="20" y2="20" />
        </>
      )}
      {type === 'volleyball' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M12 3c0 6-3 9-9 9" />
          <path d="M12 3c3 3 6 4 9 4" />
          <path d="M21 12c-6 0-9 3-9 9" />
        </>
      )}
      {type === 'handball' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <circle cx="12" cy="12" r="3" />
        </>
      )}
      {type === 'badminton' && (
        <>
          <path d="M8 4l-2 8h12l-2-8z" />
          <path d="M12 12v8" />
        </>
      )}
      {type === 'table-tennis' && (
        <>
          <circle cx="10" cy="10" r="6" />
          <line x1="14" y1="14" x2="20" y2="20" />
        </>
      )}
      {type === 'squash' && (
        <>
          <circle cx="10" cy="10" r="6" />
          <line x1="14" y1="14" x2="20" y2="20" />
        </>
      )}
      {type === 'running' && (
        <>
          <circle cx="13" cy="4" r="2" />
          <path d="M7 21l3-4 2 1 3 4" />
          <path d="M11 13l-2 4" />
          <path d="M12 8l-3 2 1 3h4l2-3" />
        </>
      )}
      {type === 'cycling' && (
        <>
          <circle cx="5.5" cy="17.5" r="3.5" />
          <circle cx="18.5" cy="17.5" r="3.5" />
          <path d="M12 17.5l-3-6 4-3 3.5 4H20" />
        </>
      )}
      {type === 'swimming' && (
        <>
          <path d="M2 18c2.5 0 2.5-1.5 5-1.5s2.5 1.5 5 1.5 2.5-1.5 5-1.5 2.5 1.5 5 1.5" />
          <path d="M2 14c2.5 0 2.5-1.5 5-1.5s2.5 1.5 5 1.5 2.5-1.5 5-1.5 2.5 1.5 5 1.5" />
        </>
      )}
    </svg>
  )
}
