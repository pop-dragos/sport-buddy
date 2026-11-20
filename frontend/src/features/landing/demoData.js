// Date fictive pentru demonstrația de pe /bun-venit. Pagina nu face nicio cerere la server,
// deci merge și fără backend. Textele și formatele sunt cele din aplicație (calendar, lobby, review).

export const ME = { username: 'ana', fullName: 'Ana Popescu' }

export const CITY = 'Baia Mare'

// aceeași ordine și aceleași coduri ca în catalog (pentru SportIcon)
export const SPORTS = [
  { code: 'football', name: 'Fotbal' },
  { code: 'basketball', name: 'Baschet' },
  { code: 'tennis', name: 'Tenis' },
  { code: 'padel', name: 'Padel' },
  { code: 'volleyball', name: 'Volei' },
  { code: 'handball', name: 'Handbal' },
  { code: 'badminton', name: 'Badminton' },
  { code: 'table-tennis', name: 'Tenis de masă' },
  { code: 'squash', name: 'Squash' },
  { code: 'running', name: 'Alergare' },
  { code: 'cycling', name: 'Ciclism' },
  { code: 'swimming', name: 'Înot' }
]

// noiembrie 2026 (luna 10 în Date, numărate de la 0)
export const MONTH = { year: 2026, month: 10 }
export const DAYS_WITH_LOBBIES = [3, 6, 8, 10, 13, 14, 15, 17, 20, 21, 22, 24, 27, 28, 29]
export const SELECTED_DAY = 14

// lobby-urile de fotbal din Baia Mare, sâmbătă 14 noiembrie; al treilea e cel ales
export const DAY_LOBBIES = [
  { id: 1, dateTime: '2026-11-14T10:00:00', field: 'Teren de fotbal – Nord', current: 10, max: 10, rating: 4.1 },
  { id: 2, dateTime: '2026-11-14T17:30:00', field: 'Teren de fotbal – Sud', current: 6, max: 10, rating: 3.7 },
  { id: 3, dateTime: '2026-11-14T19:00:00', field: 'Teren de fotbal – Centru', current: 7, max: 10, rating: 4.4 },
  { id: 4, dateTime: '2026-11-14T21:00:00', field: 'Teren de fotbal – Nord', current: 4, max: 10, rating: null }
]
export const CHOSEN_LOBBY_ID = 3

// lobby-ul ales: 4 jucători cu cont și 3 aduși de organizator = 7 din 10
export const LOBBY = {
  sport: 'Fotbal',
  field: 'Teren de fotbal – Centru',
  city: CITY,
  dateTime: '2026-11-14T19:00:00',
  confirmDeadline: '2026-11-14T07:00:00',
  host: 'andrei',
  maxPlayers: 10,
  minPlayers: 8,
  broughtPlayers: 3,
  fieldCost: 240,
  players: [
    { username: 'andrei', isHost: true, rating: 4.6, reliability: 96 },
    { username: 'vlad', rating: 4.2, reliability: 92 },
    { username: 'elena', rating: 4.8, reliability: 100 },
    { username: 'radu', rating: 3.9, reliability: 88 }
  ],
  me: { username: 'ana', rating: 4.4, reliability: 95 }
}

// prețul pe persoană, ca în backend: costul terenului împărțit la locurile ocupate, rotunjit în sus
export function pricePerPlayer(players) {
  return Math.ceil(LOBBY.fieldCost / players)
}

// notele date după meci colegilor (toți participanții cu cont, în afară de mine)
export const REVIEWS = [
  { username: 'andrei', score: 5 },
  { username: 'vlad', score: 4 },
  { username: 'elena', score: 5 },
  { username: 'radu', score: 4 }
]

// Pașii poveștii, în ordinea din aplicație, și unde încep pe scroll (0 = începutul secțiunii, 1 = sfârșitul).
// members și review nu schimbă nimic în aplicație: doar „derulează" pagina lobby-ului din fereastră,
// ca să se vadă participanții, respectiv cardul de review.
export const PHASES = [
  { name: 'home', from: 0 },
  { name: 'calendar', from: 0.08 },
  { name: 'lobby', from: 0.32 },
  { name: 'pending', from: 0.4 },
  { name: 'joined', from: 0.46 },
  { name: 'members', from: 0.52 },
  { name: 'locked', from: 0.63 },
  { name: 'finished', from: 0.7 },
  { name: 'review', from: 0.74 },
  { name: 'rated', from: 0.86 }
]

export function phaseAt(progress) {
  let current = PHASES[0].name
  for (const p of PHASES) if (progress >= p.from) current = p.name
  return current
}

// true dacă pasul `name` a fost atins în faza curentă
export function reached(phase, name) {
  return PHASES.findIndex((p) => p.name === phase) >= PHASES.findIndex((p) => p.name === name)
}

// Etapele poveștii. text: bucăți de frază; cele cu strong apar în culoarea textului, restul în gri (ca la Apple).
// range: unde stă etapa pe progresul poveștii (după secțiunea de deschidere).
export const STAGES = [
  {
    title: 'Toate meciurile din oraș. Un singur calendar.',
    text: [
      { t: 'Alegi orașul și sportul. ' },
      { t: 'Vezi imediat unde se joacă,', strong: true },
      { t: ' câți jucători sunt și de câți mai e nevoie.' }
    ],
    // pe mobil și cu reduced-motion: starea finală a etapei
    staticPhase: 'calendar',
    // calendarul deschis, toate lobby-urile zilei afișate și cel ales evidențiat
    staticProgress: 0.28,
    range: [0, 0.31]
  },
  {
    title: 'Intri în joc. Fără mesaje inutile.',
    text: [
      { t: 'Trimiți o cerere, gazda o acceptă. ' },
      { t: 'Ratingul fiecăruia e la vedere', strong: true },
      { t: ' înainte de meci.' }
    ],
    staticPhase: 'members',
    staticProgress: 0.6,
    range: [0.31, 0.61]
  },
  {
    title: 'Confirmat. Corect pentru toți.',
    text: [
      { t: 'La termen, lobby-ul se confirmă și ' },
      { t: 'prețul devine final.', strong: true },
      { t: ' După meci, fiecare primește note de la colegi.' }
    ],
    staticPhase: 'rated',
    staticProgress: 1,
    range: [0.61, 1]
  }
]

// Partea de la începutul secțiunii lipite în care se vede deschiderea (titlul mare și fereastra care crește).
export const INTRO = 0.1

// Cardurile „Pe scurt": doar lucruri pe care aplicația chiar le face.
export const HIGHLIGHTS = [
  {
    id: 'sports',
    big: '12 sporturi',
    text: 'De la fotbal și baschet la padel, squash și înot. Fiecare cu terenurile lui, în fiecare oraș.',
    accent: '#2563eb',
    wide: true
  },
  {
    id: 'price',
    big: '240 ÷ 8 = 30 RON',
    text: 'Costul terenului se împarte la jucători. Vezi prețul estimat de la început; la confirmare devine final.',
    accent: '#16a34a'
  },
  {
    id: 'deadline',
    big: '12 · 6 · 2 ore',
    text: 'Termenul de confirmare se potrivește cu meciul: 12 ore înainte dacă e peste cel puțin 3 zile, 6 ore între 1 și 3 zile, 2 ore sub o zi.',
    accent: '#ea580c'
  },
  {
    id: 'rating',
    big: '★ 4.6',
    text: 'Ratingul nu se poate ascunde. Vezi cu cine joci și cât de des vine la meciuri, înainte să intri.',
    accent: '#f59e0b'
  },
  {
    id: 'chat',
    big: 'Chat în lobby',
    text: 'Fiecare lobby are conversația lui, doar pentru cei care joacă. Plus mesaje directe între prieteni.',
    accent: '#7c3aed'
  },
  {
    id: 'cities',
    big: 'Peste 40 de orașe',
    text: 'Alegi orașul din bara de sus și vezi doar meciurile de acolo, cu vremea pe cinci zile și o recomandare pentru sală sau aer liber.',
    accent: '#0891b2',
    full: true
  }
]
