<template>
  <main class="shell page">
    <section class="hero">
      <h1>人生哲学</h1>
      <p class="sub">{{ meta.subtitle }}</p>
      <p class="en">AN EVIDENCE-BASED LEDGER · 纯离线</p>
    </section>

    <div class="stats" role="list">
      <div class="stat" role="listitem"><b>{{ meta.entries }}</b><span>条建议</span></div>
      <div class="stat" role="listitem"><b>{{ meta.gradeA }}</b><span>A 级证据</span></div>
      <div class="stat" role="listitem"><b>{{ meta.chapters }}</b><span>章节</span></div>
      <div class="stat" role="listitem"><b>100%</b><span>离线可用</span></div>
    </div>

    <div v-if="store.progress" class="resume">
      <div>
        <p class="r-label">继续阅读</p>
        <p class="r-title">{{ store.progress.title }}</p>
        <p class="r-sub">第 {{ store.progress.ch }} 节 · 第 {{ store.progress.n }} 条</p>
      </div>
      <button class="btn primary" @click="resume">继续 →</button>
    </div>

    <div class="sec-h"><h2>四大资产 · 不互相折算</h2><span class="rule" /></div>
    <div class="asset-grid">
      <button v-for="a in assets" :key="a.en" class="asset" @click="$router.push('/toc')">
        <span class="a-zh">{{ a.zh }}</span><span class="a-en">{{ a.en }}</span>
        <span class="a-desc">{{ a.desc }}</span>
        <span class="a-refs">第 {{ a.refs }} 节</span>
      </button>
    </div>

    <div class="sec-h"><h2>按处境进入</h2><span class="rule" /></div>
    <div class="quick-grid">
      <button v-for="g in groups" :key="g.name" class="quick" @click="$router.push('/toc')">
        <TicksBar :count="g.count" :width="64" />
        <span class="q-name">{{ g.name }}</span>
        <span class="q-n">{{ g.nums.length }} 节 · {{ g.count }} 条</span>
      </button>
    </div>

    <div class="cta">
      <router-link to="/toc" class="btn primary">打开全书目录 →</router-link>
    </div>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { localBook, meta } from '../api'
import { store } from '../store'
import TicksBar from '../components/TicksBar.vue'
const router = useRouter()

const assets = [
  { zh: '寿命', en: 'LIFE', desc: '活得久一点', refs: '01·02·13·16' },
  { zh: '金钱', en: 'MONEY', desc: '少花冤枉钱', refs: '05·06·07·15' },
  { zh: '时间精力', en: 'TIME', desc: '降低精神内耗', refs: '03·04·22·29' },
  { zh: '人身自由', en: 'FREEDOM', desc: '守住选择权', refs: '08·09·11·14' },
]

const groups = computed(() => {
  const map = new Map()
  for (const c of localBook.chapters) {
    if (!map.has(c.group)) map.set(c.group, { name: c.group, nums: [], count: 0 })
    const g = map.get(c.group)
    g.nums.push(c.num)
    g.count += c.count
  }
  return [...map.values()]
})

function resume() {
  const p = store.progress
  if (p) router.push({ name: 'entry', params: { num: p.ch, n: p.n } })
}
</script>

<style scoped>
.hero { padding: var(--s8) 0 var(--s6); }
h1 { font-family: var(--serif); font-size: clamp(30px, 6vw, 44px); letter-spacing: 6px; }
.sub { margin-top: var(--s3); font-family: var(--serif); font-size: 15px; letter-spacing: 2px; color: var(--ink-2); }
.en { margin-top: var(--s2); font-size: 10px; letter-spacing: 4px; color: var(--mid); }
.stats { display: flex; border: 1px solid var(--line); border-radius: var(--r); margin: var(--s6) 0; background: var(--paper-2); flex-wrap: wrap; }
.stat { flex: 1 1 22%; min-width: 90px; padding: var(--s4); border-right: 1px solid var(--line); text-align: center; }
.stat:last-child { border-right: none; }
.stat b { display: block; font-family: var(--serif); font-size: 24px; color: var(--cinnabar); }
.stat span { font-size: 11px; letter-spacing: 2px; color: var(--text-2); }
.resume { display: flex; align-items: center; justify-content: space-between; gap: var(--s3); border: 1px solid var(--cinnabar); border-radius: var(--r); padding: var(--s4); margin-bottom: var(--s6); background: var(--paper-2); }
.r-label { font-size: 10px; letter-spacing: 3px; color: var(--cinnabar); }
.r-title { font-family: var(--serif); font-size: 16px; margin-top: 2px; }
.r-sub { font-size: 11px; color: var(--mid); }
.resume .btn { flex-shrink: 0; }
.asset-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s3); }
.asset { border: 1px solid var(--line); border-radius: var(--r); background: var(--paper-2); padding: var(--s4); display: flex; flex-direction: column; gap: 2px; transition: all var(--tr); }
.asset:hover { border-color: var(--cinnabar); transform: translateY(-2px); }
.a-zh { font-family: var(--serif); font-size: 19px; letter-spacing: 3px; }
.a-en { font-size: 9px; letter-spacing: 3px; color: var(--mid); }
.a-desc { font-size: 12px; color: var(--text-2); margin-top: var(--s2); }
.a-refs { font-size: 10px; color: var(--mid); }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s3); }
.quick { border: 1px solid var(--faint); border-radius: var(--r); padding: var(--s3) var(--s4); display: flex; flex-direction: column; gap: 2px; transition: all var(--tr); }
.quick:hover { background: var(--paper-2); border-color: var(--cinnabar); }
.q-name { font-size: 13px; font-weight: 600; margin-top: var(--s2); }
.q-n { font-size: 10px; color: var(--mid); }
.cta { margin-top: var(--s8); }
.cta a { text-decoration: none; }
@media (max-width: 640px) {
  .asset-grid, .quick-grid { grid-template-columns: repeat(2, 1fr); }
  .stat { flex: 1 1 45%; border-bottom: 1px solid var(--line); }
}
</style>
