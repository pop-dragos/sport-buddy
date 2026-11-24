import { useSyncExternalStore } from 'react'
import { http } from '../../api/http'
import { currentLocale, getLang, subscribeLang } from './index'

// Moneda în care se AFIȘEAZĂ prețurile (RON, EUR sau USD). Plata rămâne mereu în RON; pentru celelalte monede
// arătăm o sumă aproximativă (≈), calculată după cursul BNR de la /catalog/exchange-rates.

const KEY = 'sb_currency'
export const CURRENCIES = ['RON', 'EUR', 'USD']

function readStored() {
  try {
    const value = localStorage.getItem(KEY)
    return CURRENCIES.includes(value) ? value : 'RON'
  } catch {
    return 'RON'
  }
}

// rates: { EUR: 0.2, USD: 0.22 } = câte unități din monedă fac 1 RON; null până vine cursul
let state = { currency: readStored(), rates: null }
const listeners = new Set()
let loading = null

function emit() {
  listeners.forEach((listener) => listener())
}

function subscribe(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function loadRates() {
  if (state.rates || loading) return loading
  loading = http('/catalog/exchange-rates')
    .then((res) => {
      state = { ...state, rates: res.rates }
      emit()
    })
    .catch(() => {
      // fără curs, prețurile rămân în RON
    })
    .finally(() => { loading = null })
  return loading
}

export function getCurrency() {
  return state.currency
}

// currency: RON / EUR / USD (și cum vin din setările de pe server)
export function setCurrency(next) {
  const currency = String(next).toUpperCase()
  if (!CURRENCIES.includes(currency) || currency === state.currency) return
  state = { ...state, currency }
  try {
    localStorage.setItem(KEY, currency)
  } catch {
    // fără stocare, moneda rămâne doar pentru această vizită
  }
  emit()
  if (currency !== 'RON') loadRates()
}

if (state.currency !== 'RON') loadRates()

function number(value, digits) {
  return new Intl.NumberFormat(currentLocale(), { minimumFractionDigits: digits, maximumFractionDigits: 2 }).format(value)
}

export function formatRon(ron) {
  return `${number(ron, 0)} RON`
}

function converted(ron) {
  const { currency, rates } = state
  if (currency === 'RON' || !rates?.[currency]) return null
  return `≈ ${number(ron * rates[currency], 2)} ${currency}`
}

// Prețul în moneda aleasă. În RON: „12 RON”; în EUR: „≈ 2,41 EUR”.
export function formatMoney(ron) {
  return converted(ron) ?? formatRon(ron)
}

// Unde se plătește de fapt (în RON) arătăm ambele: „≈ 2,41 EUR (12 RON)”.
export function formatMoneyBoth(ron) {
  const other = converted(ron)
  return other ? `${other} (${formatRon(ron)})` : formatRon(ron)
}

export function useMoney() {
  const { currency } = useSyncExternalStore(subscribe, () => state)
  // și limba, pentru că separatorul zecimal depinde de ea
  useSyncExternalStore(subscribeLang, getLang)
  return { currency, money: formatMoney, moneyBoth: formatMoneyBoth }
}
