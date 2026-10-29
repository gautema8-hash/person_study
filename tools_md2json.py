# -*- coding: utf-8 -*-
"""数据管线：HowToLiveBetter/book/*.md → book.json（后端资源 + 前端内置数据共用）"""
import re, os, glob, json, sys

SRC = r"D:/sofa/aiproject/workBuddyWork/gaoxingjiabi/HowToLiveBetter/book"
OUTS = [
    r"D:/sofa/aiproject/workBuddyWork/gaoxingjiabi/renshengzhexue/backend/data/book.json",
    r"D:/sofa/aiproject/workBuddyWork/gaoxingjiabi/renshengzhexue/frontend/src/data/book.json",
]

GROUPS = {
    "健康与生命": [1,2,13,16,24,28,34], "精力与时间": [3,4,22,29], "金钱与账目": [5,6,7,15],
    "法律与信息安全": [8,9,11,14,26], "家庭与人生阶段": [10,17,18,20,25,27,30],
    "工作与技能": [12,19,23,31], "出境与远行": [21,32], "特殊处境": [33],
}

def group_of(num):
    for g, nums in GROUPS.items():
        if num in nums: return g
    return "其他"

chapters = []
for f in sorted(glob.glob(os.path.join(SRC, "*.md"))):
    num = int(re.match(r"(\d+)", os.path.basename(f)).group(1))
    text = open(f, encoding="utf-8").read()
    title = re.search(r"^# \d+\. (.+)$", text, re.M).group(1).strip()
    intro_m = re.search(r"^# .+?\n\n(.+?)\n", text, re.M)
    intro = intro_m.group(1).strip() if intro_m else ""
    entries = []
    parts = re.split(r"(?=^### \d+\. )", text, flags=re.M)
    for p in parts:
        m = re.match(r"^### (\d+)\. (.+)$", p, re.M)
        if not m: continue
        fields = dict(re.findall(r"^- (成本|说人话|收益|证据等级|来源|备注)：(.*)$", p, re.M))
        tag_m = re.search(r"<!-- (成本标签:.*?) -->", p)
        tags = []
        if tag_m:
            for kv in tag_m.group(1).replace("成本标签:", "").split():
                k, _, v = kv.partition("=")
                tags.append({"k": k, "v": v})
        entries.append({
            "n": int(m.group(1)), "title": m.group(2).strip(),
            "cost": fields.get("成本", ""), "plain": fields.get("说人话", ""),
            "benefit": fields.get("收益", ""), "grade": fields.get("证据等级", "C"),
            "source": fields.get("来源", ""), "note": fields.get("备注", ""),
            "tags": tags,
        })
    themes = sorted({t for e in entries for t in []}) # placeholder
    chapters.append({"num": num, "title": title, "intro": intro,
                     "group": group_of(num), "count": len(entries), "entries": entries})

chapters.sort(key=lambda c: c["num"])
book = {
    "meta": {
        "name": "人生哲学", "subtitle": "用最少的钱、时间和精力，换回寿命、金钱和自由",
        "source": "HowToLiveBetter (github.com/eternity4719) · CC BY 4.0",
        "chapters": len(chapters),
        "entries": sum(c["count"] for c in chapters),
        "gradeA": sum(1 for c in chapters for e in c["entries"] if e["grade"] == "A"),
        "version": "1.0.0", "generated": "2026-10-30",
    },
    "chapters": chapters,
}
for out in OUTS:
    os.makedirs(os.path.dirname(out), exist_ok=True)
    json.dump(book, open(out, "w", encoding="utf-8"), ensure_ascii=False, separators=(",", ":"))
print("meta:", book["meta"])
print("OK ->", "; ".join(OUTS))
