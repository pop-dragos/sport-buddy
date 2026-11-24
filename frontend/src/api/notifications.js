import { http } from './http'

// ultimele 50, cele mai noi primele:
// { id, type, lobbyId, actorId, actorName, subject, createdAt, read }
export const getNotifications = () => http('/notifications')

// { count }
export const getUnreadCount = () => http('/notifications/unread-count')

export const markRead = (id) => http(`/notifications/${id}/read`, { method: 'POST' })

export const markAllRead = () => http('/notifications/read-all', { method: 'POST' })
