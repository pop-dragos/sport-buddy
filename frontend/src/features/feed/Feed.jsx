import { useEffect, useState } from 'react'
import { getFeed } from '../../api/feed'
import { errorMessage } from '../../api/http'
import { useCatalog } from '../profile/useCatalog'
import PostCard from './PostCard'
import PostComposer from './PostComposer'
import './feed.css'

const PAGE_SIZE = 20

function FeedList({ sportId, sports }) {
  const [posts, setPosts] = useState([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    getFeed({ sportId, page, size: PAGE_SIZE })
      .then((list) => {
        if (cancelled) return
        // o postare nouă împinge lista în jos, deci pagina următoare poate repeta una deja afișată
        setPosts((current) => {
          const known = new Set(current.map((p) => p.id))
          return [...current, ...list.filter((p) => !known.has(p.id))]
        })
        setHasMore(list.length === PAGE_SIZE)
        setLoading(false)
      })
      .catch((e) => {
        if (cancelled) return
        setError(errorMessage(e))
        setLoading(false)
      })
    return () => { cancelled = true }
  }, [sportId, page])

  function loadMore() {
    setLoading(true)
    setPage((p) => p + 1)
  }

  const sportName = (id) => sports.find((s) => s.id === id)?.name ?? ''

  return (
    <>
      <PostComposer
        sports={sports}
        filterSportId={sportId}
        onCreated={(created) => setPosts((current) => [created, ...current])}
      />

      {error && <div className="card err">{error}</div>}
      {!loading && !error && posts.length === 0 && (
        <div className="card mu">Nu există încă postări aici.</div>
      )}

      {posts.map((p) => <PostCard key={p.id} post={p} sportName={sportName(p.sportId)} />)}

      {hasMore && (
        <button type="button" className="b o" disabled={loading} onClick={loadMore} style={{ width: '100%' }}>
          Încarcă mai multe
        </button>
      )}
    </>
  )
}

export default function Feed() {
  const { sports } = useCatalog()
  const [sportId, setSportId] = useState(null)

  return (
    <div>
      <h3 className="feed-title">Comunitate</h3>
      <div className="r feed-chips">
        <button type="button" className={`chip ${sportId === null ? 'on' : ''}`} onClick={() => setSportId(null)}>
          Toate
        </button>
        {sports.map((s) => (
          <button
            key={s.id}
            type="button"
            className={`chip ${sportId === s.id ? 'on' : ''}`}
            onClick={() => setSportId(s.id)}
          >
            {s.name}
          </button>
        ))}
      </div>

      {/* key: la schimbarea filtrului lista pornește de la pagina 0 */}
      <FeedList key={sportId ?? 'all'} sportId={sportId} sports={sports} />
    </div>
  )
}
