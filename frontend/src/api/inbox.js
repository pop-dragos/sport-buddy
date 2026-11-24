import { http } from './http'

// conversațiile mele cu prietenii, cea mai recentă prima:
// [{ userId, username, fullName, avatarUrl, lastMessage, lastFromMe, sentAt, unread }]
export const getConversations = () => http('/messages/conversations')

// totalul mesajelor necitite, pentru insigna din header: { count }
export const getUnreadMessages = () => http('/messages/unread-count')
