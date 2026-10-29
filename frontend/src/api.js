// 数据层：内容 100% 打包内置（import 静态资源），纯离线可用。
// 后端（Java）提供同源 /api/*，仅用于 Web 模式下的状态探测与未来内容同步；
// 探测失败（离线/打包进 App）时静默回退内置数据，不阻塞 UI。
import book from './data/book.json'

export const localBook = book
export const meta = book.meta

export const chapterByNum = new Map(book.chapters.map((c) => [c.num, c]))

export function getChapter(num) {
  return chapterByNum.get(Number(num)) || null
}

export function getEntry(chNum, n) {
  const c = getChapter(chNum)
  if (!c) return null
  return c.entries.find((e) => e.n === Number(n)) || null
}

export function searchLocal(q, limit = 50) {
  const needle = String(q || '').trim().toLowerCase()
  if (!needle) return []
  const hits = []
  for (const c of book.chapters) {
    for (const e of c.entries) {
      const hay = (e.title + '\n' + e.plain + '\n' + e.benefit).toLowerCase()
      if (hay.includes(needle)) {
        hits.push({ ch: c.num, chTitle: c.title, n: e.n, title: e.title, grade: e.grade, plain: e.plain })
        if (hits.length >= limit) return hits
      }
    }
  }
  return hits
}

/** 探测后端是否在线（800ms 超时），用于首页「离线/在线」状态徽标 */
export async function probeBackend(ms = 800) {
  try {
    const ctrl = new AbortController()
    const timer = setTimeout(() => ctrl.abort(), ms)
    const res = await fetch('/api/health', { signal: ctrl.signal })
    clearTimeout(timer)
    return res.ok
  } catch {
    return false
  }
}
