const SUN = '#f59e0b'
const CLOUD = '#94a3b8'
const DROP = '#3b82f6'

function Sun() {
    return (
        <g>
            <circle cx="12" cy="12" r="4" fill={SUN} />
            <g stroke={SUN} strokeWidth="1.8" strokeLinecap="round">
                {[0, 45, 90, 135, 180, 225, 270, 315].map((angle) => (
                    <line key={angle} x1="12" y1="2.5" x2="12" y2="5" transform={`rotate(${angle} 12 12)`} />
                ))}
            </g>
        </g>
    )
}

function Cloud() {
    return (
        <path
            d="M6.5 18a4 4 0 0 1-.4-7.98 5.5 5.5 0 0 1 10.6-1.2A4.6 4.6 0 0 1 17 18H6.5z"
            fill={CLOUD}
            stroke="var(--bg)"
            strokeWidth="0.8"
        />
    )
}

export default function WeatherIcon({ condition, size = 28 }) {
    let content = null

    if (condition === 'SUNNY') {
        content = <Sun />
    } else if (condition === 'PARTLY_CLOUDY') {
        content = (
            <>
                <g transform="translate(0.6 0.6) scale(0.7)">
                    <Sun />
                </g>
                <g transform="translate(4 6) scale(0.8)">
                    <Cloud />
                </g>
            </>
        )
    } else if (condition === 'CLOUDY') {
        content = <Cloud />
    } else if (condition === 'RAIN') {
        content = (
            <>
                <g transform="translate(0 -3)">
                    <Cloud />
                </g>
                <g stroke={DROP} strokeWidth="1.8" strokeLinecap="round">
                    <line x1="8" y1="17.5" x2="7" y2="21" />
                    <line x1="12" y1="17.5" x2="11" y2="21" />
                    <line x1="16" y1="17.5" x2="15" y2="21" />
                </g>
            </>
        )
    }

    return (
        <svg width={size} height={size} viewBox="0 0 24 24" aria-hidden="true">
            {content}
        </svg>
    )
}