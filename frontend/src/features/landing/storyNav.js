import { INTRO } from './demoData'

// „Vezi cum funcționează": duce la prima etapă a poveștii.
// În varianta lipită, prima etapă începe după deschidere (INTRO), nu la începutul secțiunii.
export function showStory(smooth = true) {
  const behavior = smooth ? 'smooth' : 'auto'
  const story = document.querySelector('.story')
  if (story) {
    const top = story.getBoundingClientRect().top + window.scrollY
    const travel = story.offsetHeight - window.innerHeight
    window.scrollTo({ top: top + (INTRO + 0.01) * travel, behavior })
    return
  }
  document.getElementById('cum-functioneaza')?.scrollIntoView({ behavior, block: 'start' })
}
