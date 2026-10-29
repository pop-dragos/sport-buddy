import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth'

export default function RequireAuth() {
  const { status, expiredUserId } = useAuth()
  const location = useLocation()

  if (status === 'loading') return null
  if (status === 'guest') {
    // fromUserId: cui îi aparține pagina reținută (null = nu știm, de exemplu un link deschis direct)
    return <Navigate to="/login" replace state={{ from: location, fromUserId: expiredUserId }} />
  }
  return <Outlet />
}
