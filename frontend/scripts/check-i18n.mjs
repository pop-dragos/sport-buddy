// Verifică traducerile: `npm run check:i18n`
//
// 1. Fiecare text din t('...') / tn(n, '...', '...') trebuie să aibă traducere în src/shared/i18n/en/*.js.
// 2. Căutăm texte românești rămase în afara lui t(): diacritice în cod, text între etichete JSX și valori de
//    placeholder / title / aria-label / alt.
// 3. Avertizăm la intrările din dicționar pe care nu le folosește nimic (de obicei texte rămase după o modificare).
//
// Iese cu cod 1 dacă lipsește vreo traducere sau rămâne vreun text netradus, ca să poată rula și în CI.
import { readdirSync, readFileSync, statSync } from 'node:fs'
import { join, relative } from 'node:path'
import { pathToFileURL } from 'node:url'

const SRC = new URL('../src/', import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1')

// fișiere care păstrează deliberat text românesc, cu motivul
const IGNORED_FILES = [
  // documentele legale sunt scrise integral în fiecare limbă (nu fraza cu frază), în același fișier
  join('features', 'legal', 'legalContent.js')
]
// nume proprii și adrese fictive care nu se traduc
const ALLOWED = [
  /sportbuddy\.local/,
  /<span>portBuddy<\/span>/,
  /hero-eyebrow">SportBuddy</,
  /aria-label="SportBuddy"/,
  /name="vlad"/
]
// Un rând poate fi scutit cu comentariul `i18n-ignore: motiv` (nume proprii, numele limbilor etc.)

function walk(dir) {
  const out = []
  for (const name of readdirSync(dir)) {
    const path = join(dir, name)
    if (statSync(path).isDirectory()) out.push(...walk(path))
    else if (/\.(jsx?|mjs)$/.test(name)) out.push(path)
  }
  return out
}

// codul sistemului de traducere (shared/i18n) conține doar exemple în comentarii, nu texte de interfață
const files = walk(SRC).filter((f) => !f.includes(join('shared', 'i18n')))
const dictionary = (await import(pathToFileURL(join(SRC, 'shared/i18n/en.js')).href)).default

const unescape = (s) => s.replace(/\\(['"`\\])/g, '$1').replace(/\\n/g, '\n')
const LIT = String.raw`(['"\x60])((?:\\.|(?!\1)[^\\\n])*)\1`
const tCall = new RegExp(String.raw`(?<![\w.])(?:t|tr|tx)\(\s*${LIT}`, 'g')
const tnCall = new RegExp(String.raw`(?<![\w.])(?:tn|trn)\(\s*[^,()]+,\s*${LIT}\s*,\s*${LIT.replace(/\\1/g, '\\3')}`, 'g')

const used = new Set()
const missing = []
const record = (file, key, line) => {
  const text = unescape(key)
  used.add(text)
  if (!(text in dictionary)) missing.push(`${relative(SRC, file)}:${line}  ${JSON.stringify(text)}`)
}
const lineOf = (text, index) => text.slice(0, index).split('\n').length

for (const file of files) {
  const text = readFileSync(file, 'utf8')
  for (const m of text.matchAll(tCall)) record(file, m[2], lineOf(text, m.index))
  for (const m of text.matchAll(tnCall)) {
    record(file, m[2], lineOf(text, m.index))
    record(file, m[4], lineOf(text, m.index))
  }
}

// --- texte românești rămase în afara lui t() ---
const DIACRITICS = /[ăâîșțĂÂÎȘȚşţ]/
const leftovers = []
for (const file of files) {
  if (IGNORED_FILES.some((f) => file.endsWith(f))) continue
  const lines = readFileSync(file, 'utf8').split('\n')
  let inComment = false
  lines.forEach((raw, i) => {
    const line = raw.trim()
    // comentarii JSX / bloc pe mai multe rânduri
    if (inComment) {
      if (line.includes('*/')) inComment = false
      return
    }
    if ((line.startsWith('{/*') || line.startsWith('/*')) && !line.includes('*/')) {
      inComment = true
      return
    }
    if (line.startsWith('//') || line.startsWith('*') || line.startsWith('/*') || line.startsWith('{/*')) return
    // fără comentariile de la sfârșitul rândului și fără textul din t()
    if (line.includes('throw new Error(') || line.includes('i18n-ignore') || ALLOWED.some((re) => re.test(line))) return
    const code = raw.replace(/\s\/\/.*$/, '').replace(new RegExp(tCall.source, 'g'), '').replace(new RegExp(tnCall.source, 'g'), '').replace(/\{\/\*.*?\*\/\}/g, '')
    const where = `${relative(SRC, file)}:${i + 1}  ${line.slice(0, 110)}`
    if (DIACRITICS.test(code)) leftovers.push(where)
    else if (/<(?:[A-Za-z][\w.]*)[^<>]*>[^<>{}]*[A-Za-z]{3,}[^<>{}]*<\//.test(code)) leftovers.push(where)
    else if (/\b(?:placeholder|title|aria-label|alt)="[^"{]*[A-Za-z]{3,}/.test(code)) leftovers.push(where)
  })
}

// fișierele de dicționar care încep cu „// dinamic” conțin texte alese la rulare (de exemplu numele sporturilor
// venite din server), deci nu apar ca t('...') în cod
const dynamicKeys = new Set()
for (const name of readdirSync(join(SRC, 'shared/i18n/en'))) {
  const text = readFileSync(join(SRC, 'shared/i18n/en', name), 'utf8')
  if (text.startsWith('// dinamic')) {
    for (const m of text.matchAll(/^ {2}"((?:[^"\\]|\\.)*)":/gm)) dynamicKeys.add(JSON.parse(`"${m[1]}"`))
  }
}
const unused = Object.keys(dictionary).filter((k) => !used.has(k) && !dynamicKeys.has(k))

let failed = false
if (missing.length) {
  failed = true
  console.log(`\nFără traducere în engleză (${missing.length}):`)
  missing.forEach((m) => console.log('  ' + m))
}
if (leftovers.length) {
  failed = true
  console.log(`\nText posibil netradus, în afara lui t() (${leftovers.length}):`)
  leftovers.forEach((m) => console.log('  ' + m))
}
if (unused.length) {
  console.log(`\nIntrări din dicționar neîntâlnite în cod (${unused.length}), poate folosite dinamic:`)
  unused.slice(0, 40).forEach((k) => console.log('  ' + JSON.stringify(k)))
  if (unused.length > 40) console.log(`  ... și încă ${unused.length - 40}`)
}
console.log(failed ? '\nVerificarea a eșuat.' : '\nToate textele sunt traduse.')
process.exit(failed ? 1 : 0)
