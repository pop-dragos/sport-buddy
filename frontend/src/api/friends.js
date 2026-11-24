import { http } from './http'

const send = (method, path, body) =>
  http(path, { method, body: body === undefined ? undefined : JSON.stringify(body) })

export const getFriends = () => http('/friends')

// prietenii altcuiva; 403 FRIEND_LIST_HIDDEN dacă setările lui nu permit
export const getFriendsOf = (userId) => http(`/friends/of/${userId}`)

export const getSuggestions = () => http('/friends/suggestions')

export const getRequests = () => http('/friends/requests')

export const sendRequest = (userId) => send('POST', '/friends/requests', { userId })

export const acceptRequest = (requestId) => send('POST', `/friends/requests/${requestId}/accept`)

export const rejectRequest = (requestId) => send('POST', `/friends/requests/${requestId}/reject`)

export const cancelRequest = (requestId) => send('DELETE', `/friends/requests/${requestId}`)

export const removeFriend = (userId) => send('DELETE', `/friends/${userId}`)

export const getStatus = (userId) => http(`/friends/status/${userId}`)

// starea față de mai mulți utilizatori deodată: { [userId]: { status, requestId?, canAdd? } }
export const getStatuses = (userIds) => http(`/friends/statuses?ids=${userIds.join(',')}`)

export function getMessages(userId, afterId) {
  return http(`/friends/${userId}/messages${afterId ? `?after=${afterId}` : ''}`)
}

export const sendMessage = (userId, message) => send('POST', `/friends/${userId}/messages`, { message })
