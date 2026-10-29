import { useState } from 'react'
import { Link } from 'react-router-dom'
import { addComment, getComments, toggleLike } from '../../api/feed'
import { errorMessage } from '../../api/http'
import Avatar from '../../shared/Avatar'
import { tagLabel } from './tags'

function commentsLabel(n) {
  if (n === 0) return 'Niciun comentariu'
  return n === 1 ? '1 comentariu' : `${n} comentarii`
}

export default function PostCard({ post, sportName }) {
  const [likes, setLikes] = useState({ liked: post.likedByMe, count: post.likeCount })
  const [commentCount, setCommentCount] = useState(post.commentCount)
  // null: comentariile nu au fost încă cerute; se încarcă doar la deschidere
  const [comments, setComments] = useState(null)
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function like() {
    setBusy(true)
    setError('')
    try {
      const r = await toggleLike(post.id)
      setLikes({ liked: r.liked, count: r.likeCount })
    } catch (e) {
      setError(errorMessage(e))
    }
    setBusy(false)
  }

  async function toggleComments() {
    if (open) return setOpen(false)
    setOpen(true)
    if (comments) return
    try {
      setComments(await getComments(post.id))
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function send() {
    if (!draft.trim()) return
    setError('')
    try {
      const added = await addComment(post.id, draft.trim())
      // dacă lista nu era încă încărcată, o cerem întreagă ca să nu lipsească cele vechi
      setComments(comments ? [...comments, added] : await getComments(post.id))
      setCommentCount((n) => n + 1)
      setOpen(true)
      setDraft('')
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const { author } = post

  return (
    <div className="card">
      <div className="top">
        <Link to={`/profile/${author.id}`} className="r feed-author">
          <Avatar name={author.fullName} url={author.avatarUrl} />
          {author.username}
        </Link>
        <span className={`tag feed-${post.tag}`}>{sportName} · {tagLabel(post.tag)}</span>
      </div>

      <p className="feed-text">{post.content}</p>

      <div className="r">
        <button type="button" className="b o" disabled={busy} onClick={like} aria-pressed={likes.liked}>
          {likes.liked ? '♥' : '♡'} {likes.count}
        </button>
        <button type="button" className="feed-link" onClick={toggleComments}>
          {commentsLabel(commentCount)}
        </button>
      </div>

      {open && comments?.map((c) => (
        <div className="feed-comment" key={c.id}>
          <b>{c.author.username}:</b> {c.content}
        </div>
      ))}

      <div className="r" style={{ marginTop: 8 }}>
        <input
          placeholder="Scrie un comentariu"
          maxLength={500}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') send() }}
        />
        <button type="button" className="b o" onClick={send}>Trimite</button>
      </div>

      {error && <div className="err" style={{ marginTop: 8 }}>{error}</div>}
    </div>
  )
}
