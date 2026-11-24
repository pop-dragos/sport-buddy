// Limba aplicației (română / engleză).
//
// Textele din cod sunt scrise în română și servesc drept cheie: t('Conectează-te') întoarce textul românesc
// sau, în engleză, traducerea lui din ./en. Un text fără traducere rămâne în română (se vede, nu strică nimic);
// scriptul `npm run check:i18n` găsește textele rămase netraduse.
//
// Limba se ține minte în browser (sb_lang) și, după conectare, în setările contului (language).
import en from './en'

const KEY = 'sb_lang'

function readStored() {
  try {
    return localStorage.getItem(KEY) === 'en' ? 'en' : 'ro'
  } catch {
    return 'ro'
  }
}

let current = readStored()
const listeners = new Set()

if (typeof document !== 'undefined') document.documentElement.lang = current

// Limba aleasă de un vizitator neconectat (comutatorul de pe login / prezentare). La conectare, alegerea lui
// se salvează în cont, în loc să fie înlocuită de limba veche din cont.
let chosenByGuest = false

export function markLanguageChoice() {
  chosenByGuest = true
}

export function takeLanguageChoice() {
  const chosen = chosenByGuest
  chosenByGuest = false
  return chosen
}

export function getLang() {
  return current
}

export function subscribeLang(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

// next: 'ro' sau 'en' (sau RO / EN, cum vin din setările de pe server)
export function setLang(next) {
  const lang = String(next).toLowerCase() === 'en' ? 'en' : 'ro'
  if (lang === current) return
  current = lang
  try {
    localStorage.setItem(KEY, lang)
  } catch {
    // fără stocare, limba rămâne doar pentru această vizită
  }
  document.documentElement.lang = lang
  listeners.forEach((listener) => listener())
}

function fill(text, params) {
  if (!params) return text
  return text.replace(/\{(\w+)\}/g, (whole, name) => (params[name] ?? whole))
}

// t('Ai {n} cereri', { n: 3 })
export function tr(key, params) {
  const text = current === 'en' ? (en[key] ?? key) : key
  return fill(text, params)
}

// Plural: trn(n, '{n} jucător', '{n} jucători'). Ambele forme sunt chei în dicționar; {n} se completează.
export function trn(n, one, other, params) {
  return tr(n === 1 ? one : other, { n, ...params })
}

// Numele terenurilor din catalog au forma „tip – zonă" („Teren de fotbal – Nord"): tipul și zona se traduc separat.
// Un nume care nu are forma asta se traduce întreg (sau rămâne cum e).
export function trField(name) {
  const [type, area, ...rest] = String(name ?? '').split(' – ')
  if (!area || rest.length) return tr(name)
  return `${tr(type)} – ${tr(area)}`
}

// Marchează un text care se traduce la afișare (t(text)), când el stă într-un fișier de date, nu într-o componentă.
// Nu face nimic la rulare; scriptul check:i18n îl tratează ca pe t('...'), deci verifică și aceste texte.
export const tx = (text) => text

export function currentLocale() {
  return current === 'en' ? 'en-GB' : 'ro-RO'
}
