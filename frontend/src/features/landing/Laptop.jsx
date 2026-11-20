// Rama de laptop în jurul ferestrei aplicației: ecran cu margine neagră și camera sus, baza dedesubt.
// Desenată plat, ca în pozele de produs; în povestea lipită are mărimea fixă din ScrollStory (LAPTOP).
export default function Laptop({ fluid, children }) {
  return (
    <div className={`laptop${fluid ? ' fluid' : ''}`}>
      <div className="laptop-screen">
        <i className="laptop-camera" />
        {children}
      </div>
      <div className="laptop-base"><i /></div>
    </div>
  )
}
