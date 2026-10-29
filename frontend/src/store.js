// 本机缓存层（纯离线）：localStorage 持久化 收藏 / 阅读进度 / 历史 / 设置。
// key 统一 rszx.v1.*，内含数据版本，升级内容版本时可迁移清理。
import { reactive, watch } from 'vue'
import { meta } from './api'

const NS = 'rszx.v1.'

function load(key, dft, validate) {
  try {
    const raw = localStorage.getItem(NS + key)
    if (!raw) return dft
    const val = JSON.parse(raw)
    // P2-4：旧版本残留数据形状不符时回默认值，防白屏
    return validate && !validate(val) ? dft : val
  } catch {
    return dft
  }
}

function save(key, val) {
  try { localStorage.setItem(NS + key, JSON.stringify(val)) } catch { /* 存储满时静默 */ }
}

export const store = reactive({
  favorites: load('favs', [], (v) => Array.isArray(v)),            // ["5:1", "1:1", ...]
  progress: load('progress', null, (v) => v && typeof v.ch === 'number'),       // { ch, n, title, ts }
  history: load('history', [], (v) => Array.isArray(v)),           // 最近阅读，最新在前，≤20
  settings: load('settings', { font: 'm' }, (v) => v && typeof v === 'object'),
  online: null,                           // null=探测中, true=在线, false=离线
})

export function toggleFav(ch, n) {
  const id = `${ch}:${n}`
  const i = store.favorites.indexOf(id)
  if (i >= 0) store.favorites.splice(i, 1)
  else store.favorites.unshift(id)
}

export function isFav(ch, n) {
  return store.favorites.includes(`${ch}:${n}`)
}

export function recordRead(ch, n, title) {
  store.progress = { ch, n, title, ts: Date.now() }
  store.history = [
    { ch, n, title, ts: Date.now() },
    ...store.history.filter((h) => !(h.ch === ch && h.n === n)),
  ].slice(0, 20)
}

watch(() => store.favorites, (v) => save('favs', v), { deep: true })
watch(() => store.progress, (v) => save('progress', v), { deep: true })
watch(() => store.history, (v) => save('history', v), { deep: true })
watch(() => store.settings, (v) => save('settings', v), { deep: true })

export { meta }
