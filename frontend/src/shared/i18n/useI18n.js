import { useSyncExternalStore } from 'react'
import { currentLocale, getLang, setLang, subscribeLang, tr, trn } from './index'

// Componentele care afișează text folosesc hook-ul: la schimbarea limbii se redesenează singure.
export function useI18n() {
  const lang = useSyncExternalStore(subscribeLang, getLang)
  return { lang, t: tr, tn: trn, locale: currentLocale(), setLang }
}

export function useT() {
  return useI18n().t
}
