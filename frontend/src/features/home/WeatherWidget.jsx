import { useEffect, useState } from 'react'
import { getWeather } from '../../api/weather'
import WeatherIcon from './WeatherIcon'
import './weather.css'

function dayLabel(dateStr, index) {
    if (index === 0) return 'Azi'
    return new Date(dateStr + 'T00:00').toLocaleDateString('ro-RO', { weekday: 'short' })
}

export default function WeatherWidget({ cityId, cityName }) {
    const [data, setData] = useState(null)
    const [error, setError] = useState(false)

    useEffect(() => {
        let cancelled = false
        setData(null)
        setError(false)

        getWeather(cityId, 5)
            .then((result) => {
                if (!cancelled) setData(result)
            })
            .catch(() => {
                if (!cancelled) setError(true)
            })

        return () => {
            cancelled = true
        }
    }, [cityId])

    return (
        <div className="card">
            <div className="mu">Vremea în {cityName}</div>

            {error && <div className="mu">Prognoza nu este disponibilă acum.</div>}
            {!error && !data && <div className="mu">Se încarcă...</div>}

            {data && (
                <>
                    <div className="weather-days">
                        {data.days.map((day, i) => (
                            <div key={day.date} className="weather-day">
                                <div className="mu">{dayLabel(day.date, i)}</div>
                                <WeatherIcon condition={day.condition} />
                                <b>{day.tempMax}°</b>
                                <div className="mu">{day.tempMin}°</div>
                                <div className="mu">{day.rainChancePercent}%</div>
                            </div>
                        ))}
                    </div>
                    <div>{data.recommendation}</div>
                </>
            )}
        </div>
    )
}