import { useEffect, useState } from 'react'
import { getDashboard } from '../../api/dashboard'
import { getSettings } from '../../api/users'
import { useAuth } from '../auth/useAuth'
import { useCity } from '../catalog/useCity'
import Feed from '../feed/Feed'
import NextMatchCard from './NextMatchCard'
import ProfileCompletionCard from './ProfileCompletionCard'
import RecommendedCard from './RecommendedCard'
import TodoCard from './TodoCard'
import TopPlayersCard from './TopPlayersCard'
import WeatherWidget from './WeatherWidget'
import './home.css'

export default function HomePage() {
    const { user } = useAuth()
    const { city } = useCity()
    const [dashboard, setDashboard] = useState(null)
    // null cât timp nu știm setarea; widget-ul apare doar după, ca să nu clipească
    const [showWeather, setShowWeather] = useState(null)

    useEffect(() => {
        let cancelled = false
        getSettings()
            .then((s) => { if (!cancelled) setShowWeather(s.showWeather) })
            .catch(() => { if (!cancelled) setShowWeather(true) })
        return () => { cancelled = true }
    }, [])

    useEffect(() => {
        if (!city) return
        let cancelled = false
        getDashboard(city.id)
            .then((d) => { if (!cancelled) setDashboard(d) })
            .catch(() => { if (!cancelled) setDashboard(null) })
        return () => { cancelled = true }
    }, [city])

    const firstName = user.fullName.split(' ')[0]

    return (
        <div className="home-cols">
            <div>
                <h3 className="home-greeting">Bună, {firstName}</h3>
                <ProfileCompletionCard completion={user.profileCompletion} />

                {dashboard && (
                    <div className="g">
                        <NextMatchCard match={dashboard.nextMatch} />
                        <TodoCard todo={dashboard.todo} />
                    </div>
                )}

                {showWeather && city && <WeatherWidget cityId={city.id} cityName={city.name} />}

                {dashboard && (
                    <>
                        <RecommendedCard lobbies={dashboard.recommended} />
                        <TopPlayersCard players={dashboard.topPlayers} />
                    </>
                )}
            </div>

            <div>
                {/* Feed-ul comunității (modulul feed, persoana A) */}
                <Feed />
            </div>
        </div>
    )
}
