import WeatherWidget from './WeatherWidget'

// TEMPORAR
const TEMP_CITY = { id: 1, name: 'Baia Mare' }

export default function HomePage() {
    return (
        <div>
            <WeatherWidget cityId={TEMP_CITY.id} cityName={TEMP_CITY.name} />
        </div>
    )
}