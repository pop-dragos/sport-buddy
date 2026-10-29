import { http } from './http'

const post = (path, body) =>
  http(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) })

export function getFeed({ sportId, page = 0, size = 20 }) {
  const sport = sportId ? `&sportId=${sportId}` : ''
  return http(`/feed?page=${page}&size=${size}${sport}`)
}

export const createPost = (content, sportId, tag) => post('/feed', { content, sportId, tag })

export const toggleLike = (postId) => post(`/feed/${postId}/like`)

export const getComments = (postId) => http(`/feed/${postId}/comments`)

export const addComment = (postId, content) => post(`/feed/${postId}/comments`, { content })
