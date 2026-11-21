import { useNavigate } from 'react-router-dom'

// Primele două rânduri apar mereu, ca în prototip; celelalte doar când au ceva.
export default function TodoCard({ todo }) {
    const navigate = useNavigate()
    const rows = [
        ['Cereri de aprobat', todo.pendingRequests, true],
        ['Review-uri de dat', todo.reviewsToGive, true],
        ['Prezență de bifat', todo.attendanceToSave, false],
        ['Plăți de făcut', todo.paymentsToMake, false]
    ].filter(([, count, always]) => always || count > 0)

    return (
        <div className="card">
            <div className="mu">De făcut</div>
            {rows.map(([label, count]) => (
                <div key={label} className="pl todo-row">
                    <span>{label}</span>
                    <button className="chip todo-count" onClick={() => navigate('/agenda')}>{count}</button>
                </div>
            ))}
        </div>
    )
}
