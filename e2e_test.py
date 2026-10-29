# -*- coding: utf-8 -*-
"""人生哲学 App · E2E 测试：四级链路 / 缓存 / 检索 / 离线 / 4 类机型视口"""
from playwright.sync_api import sync_playwright
import pathlib, json

BASE = "http://localhost:8080"
shots = pathlib.Path("docs/screenshots")
shots.mkdir(parents=True, exist_ok=True)
results, errors = [], []

def check(name, cond, detail=""):
    results.append((name, bool(cond), detail))
    print(("PASS" if cond else "FAIL"), name, detail)

with sync_playwright() as p:
    browser = p.chromium.launch()

    # ═══ 功能链路（桌面视口）═══
    page = browser.new_page(viewport={"width": 1280, "height": 900})
    page.on("pageerror", lambda e: errors.append(f"pageerror: {e}"))
    page.on("console", lambda m: errors.append(f"console: {m.text}") if m.type == "error" else None)
    page.goto(BASE + "/", wait_until="networkidle")

    check("L1 首页标题", page.locator("h1").inner_text() == "人生哲学")
    check("L1 统计 767 条", "767" in page.locator(".stats").inner_text())
    check("L1 在线徽标(后端可达)", page.locator(".net.on").count() == 1)

    # L1 → L2 → L3 → L4
    page.click("text=打开全书目录")
    page.wait_for_selector(".ch-row")
    check("L2 章节 34 行", page.locator(".ch-row").count() == 34)
    page.click("text=不要浪费钱")
    page.wait_for_selector(".entry-row")
    check("L3 第5节 51 条目行", page.locator(".entry-row").count() == 51)
    page.click("text=关掉所有自动续费，改为到期手动续")
    page.wait_for_selector(".say")
    check("L4 说人话卡片", "订阅" in page.locator(".say").inner_text())
    check("L4 证据徽章 C", page.locator(".e-meta .grade-C").count() == 1)

    # 收藏 → 本机缓存持久化
    page.click(".fav")
    page.wait_for_timeout(200)
    fav_state = page.evaluate("JSON.parse(localStorage.getItem('rszx.v1.favs'))")
    check("收藏写入 localStorage", fav_state == ["5:1"], str(fav_state))

    # 阅读进度缓存
    prog = page.evaluate("JSON.parse(localStorage.getItem('rszx.v1.progress'))")
    check("阅读进度缓存", prog and prog.get("ch") == 5 and prog.get("n") == 1, str(prog)[:60])

    # 上/下一条翻页
    page.click("text=下一条 →")
    page.wait_for_timeout(300)
    check("翻页到条 2", "#/entry/5/2" in page.url)

    # Esc 返回? (hash 路由下用面包屑验证)
    page.click("text=第 5 节")
    page.wait_for_timeout(300)
    check("面包屑回 L3", "#/chapter/5" in page.url)

    # 检索（离线本地检索）
    page.fill("input[type=search]", "押金")
    page.press("input[type=search]", "Enter")
    page.wait_for_selector(".hit")
    n_hits = page.locator(".hit").count()
    check("检索「押金」有结果", n_hits > 0, f"{n_hits} hits")
    page.click(".hit >> nth=0")
    page.wait_for_timeout(300)
    check("检索结果跳转条目", "#/entry/" in page.url)

    # 404 路由兜底
    page.goto(BASE + "/#/entry/99/99", wait_until="networkidle")
    check("非法条目 → 空态兜底", page.locator(".empty").count() == 1)
    page.screenshot(path=str(shots / "e2e-空态.png"))
    page.close()

    # ═══ 离线验证：新页面缓存命中后断网 ═══
    offline = browser.new_page(viewport={"width": 390, "height": 844})
    offline.goto(BASE + "/", wait_until="networkidle")
    offline.wait_for_timeout(800)  # 等SW缓存
    sw_active = offline.evaluate("navigator.serviceWorker.getRegistration().then(r => !!r || false)")
    check("Service Worker 注册", bool(sw_active), str(sw_active))
    context = offline.context
    page.close() if False else None
    offline.context.set_offline(True)
    try:
        offline.goto(BASE + "/#/toc", wait_until="domcontentloaded")
        offline.wait_for_timeout(600)
        body = offline.locator("body").inner_text()
        check("断网后目录仍可渲染(离线缓存)", "全书目录" in body or "健康与生命" in body)
    except Exception as e:
        check("断网后目录仍可渲染(离线缓存)", False, str(e)[:80])
    offline.screenshot(path=str(shots / "e2e-离线模式.png"))
    offline.close()

    # ═══ 四类机型视口适配 ═══
    devices = [
        ("android-small-360x800", 360, 800, True),   # Android 小屏
        ("iphone-390x844", 390, 844, False),          # iPhone 15
        ("ipad-768x1024", 768, 1024, False),          # iPad
        ("desktop-1440x900", 1440, 900, False),       # 桌面
    ]
    for name, w, h, dpr3 in devices:
        pg = browser.new_page(viewport={"width": w, "height": h}, device_scale_factor=2)
        pg.goto(BASE + "/", wait_until="networkidle")
        # 无横向滚动 = 适配
        overflow = pg.evaluate("document.documentElement.scrollWidth > document.documentElement.clientWidth + 1")
        check(f"机型 {name} 无横向溢出", not overflow)
        pg.screenshot(path=str(shots / f"viewport-{name}.png"))
        # 移动机型再截一张条目页
        if w < 800:
            pg.goto(BASE + "/#/entry/1/1", wait_until="networkidle")
            overflow2 = pg.evaluate("document.documentElement.scrollWidth > document.documentElement.clientWidth + 1")
            check(f"机型 {name} 条目页无溢出", not overflow2)
            pg.screenshot(path=str(shots / f"viewport-{name}-entry.png"))
        pg.close()

    browser.close()

print("\n═══ 测试汇总 ═══")
passed = sum(1 for _, ok, _ in results if ok)
print(f"通过 {passed}/{len(results)}")
if errors:
    print("JS 错误:")
    for e in errors[:10]: print(" ", e)
else:
    print("控制台/页面 JS 错误: 0")
json.dump({"passed": passed, "total": len(results), "js_errors": len(errors)},
          open("docs/test-report.json", "w"), ensure_ascii=False)
exit(0 if passed == len(results) and not errors else 1)
