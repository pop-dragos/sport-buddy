import { http } from './http'

// afterId: doar mesajele mai noi decât el (interogarea la 3 secunde); fără el, ultimele 100
export function getMessages(lobbyId, afterId) {
    const after = afterId ? `?after=${afterId}` : ''
    return http(`/lobbies/${lobbyId}/messages${after}`)
}

export function sendMessage(lobbyId, message) {
    return http(`/lobbies/${lobbyId}/messages`, { method: 'POST', body: JSON.stringify({ message }) })
}

// { [lobbyId]: câte mesaje necitite }, doar pentru lobby-urile mele cu mesaje noi
export function getChatUnread() {
    return http('/lobbies/mine/chat-unread')
}
