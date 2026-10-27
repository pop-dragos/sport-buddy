import { useCity } from '../catalog/useCity'
import WeatherWidget from './WeatherWidget'

export default function HomePage() {
    const { city } = useCity()

    return (
        <div>
            {city && <WeatherWidget cityId={city.id} cityName={city.name} />}
        </div>
    )
}