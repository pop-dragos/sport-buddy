import GoogleButton from './GoogleButton'

const GoogleLogo = (
  <svg width="18" height="18" viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
  </svg>
)

const AppleLogo = (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
    <path d="M16.37 12.64c-.02-2.2 1.8-3.26 1.88-3.31-1.02-1.5-2.62-1.7-3.19-1.73-1.36-.14-2.65.8-3.34.8-.69 0-1.75-.78-2.88-.76-1.48.02-2.85.86-3.61 2.19-1.54 2.67-.39 6.62 1.1 8.79.73 1.06 1.6 2.25 2.74 2.21 1.1-.04 1.52-.71 2.85-.71 1.33 0 1.7.71 2.87.69 1.19-.02 1.94-1.08 2.66-2.15.84-1.23 1.19-2.42 1.2-2.48-.03-.01-2.3-.88-2.32-3.5zM14.2 6.13c.6-.74 1.01-1.76.9-2.78-.87.04-1.92.58-2.54 1.31-.56.65-1.05 1.69-.92 2.69.97.07 1.96-.49 2.56-1.22z" />
  </svg>
)

// googleClientId: cu el, butonul Google e cel real; fără el (neconfigurat), conectarea Google e simulată.
// onGoogleCredential primește token-ul dat de Google; onPick(provider) e pentru conectarea simulată.
export default function SocialButtons({ label, onPick, onGoogleCredential, googleClientId, googleText, disabled }) {
  return (
    <>
      <div className="sep">{label}</div>
      {googleClientId ? (
        <GoogleButton clientId={googleClientId} onCredential={onGoogleCredential} text={googleText} disabled={disabled} />
      ) : (
        <button type="button" className="soc" disabled={disabled} onClick={() => onPick('GOOGLE')}>
          {GoogleLogo}Google
        </button>
      )}
      <button type="button" className="soc" disabled={disabled} onClick={() => onPick('ICLOUD')}>
        {AppleLogo}iCloud
      </button>
    </>
  )
}
