package com.rszx;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 书目数据仓库：加载 book.json（Nashorn JSON.parse），提供章节/条目/检索查询。
 * 线程安全：初始化后只读。
 */
public final class Book {

    private final Map<String, Object> meta;
    private final List<Map<String, Object>> chapters;
    private final Map<Integer, Map<String, Object>> chapterByNum;
    private final long loadedAt;

    private Book(Map<String, Object> root) {
        this.meta = asMap(root.get("meta"));
        List<Map<String, Object>> chs = new ArrayList<>();
        for (Object o : asList(root.get("chapters"))) chs.add(asMap(o));
        chs.sort((a, b) -> num(a) - num(b));
        this.chapters = Collections.unmodifiableList(chs);
        Map<Integer, Map<String, Object>> idx = new LinkedHashMap<>();
        for (Map<String, Object> c : chapters) idx.put(num(c), c);
        this.chapterByNum = idx;
        this.loadedAt = System.currentTimeMillis();
    }

    public static Book load(Path jsonPath) throws Exception {
        byte[] raw = Files.readAllBytes(jsonPath);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        if (engine == null) throw new IllegalStateException("Nashorn 不可用（需要 JDK8 自带 JS 引擎）");
        String json = new String(raw, StandardCharsets.UTF_8);
        // P0-1 修复：JSON 作为【数据】传入解析器，绝不拼接为 JS 源码执行（防数据驱动代码注入）
        engine.put("__rszx_json", json);
        Object parsed = engine.eval("JSON.parse(__rszx_json);");
        return new Book(toJava(parsed));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toJava(Object o) {
        return (Map<String, Object>) o;
    }

    // ---- 查询 ----
    public Map<String, Object> meta() { return meta; }
    public List<Map<String, Object>> chapters() { return chapters; }
    public Map<String, Object> chapter(int num) { return chapterByNum.get(num); }

    public Map<String, Object> entry(int chNum, int n) {
        Map<String, Object> c = chapter(chNum);
        if (c == null) return null;
        for (Object o : asList(c.get("entries"))) {
            Map<String, Object> e = asMap(o);
            if (((Number) e.get("n")).intValue() == n) return e;
        }
        return null;
    }

    /** 全文检索：匹配条目标题/说人话/收益（不分大小写）。q 为空返回空列表。 */
    public List<Map<String, Object>> search(String q, int limit) {
        List<Map<String, Object>> hits = new ArrayList<>();
        if (q == null || q.trim().isEmpty()) return hits;
        String needle = q.trim().toLowerCase();
        for (Map<String, Object> c : chapters) {
            for (Object o : asList(c.get("entries"))) {
                Map<String, Object> e = asMap(o);
                String hay = (str(e, "title") + "\n" + str(e, "plain") + "\n" + str(e, "benefit")).toLowerCase();
                if (hay.contains(needle)) {
                    Map<String, Object> hit = new LinkedHashMap<>();
                    hit.put("ch", num(c)); hit.put("chTitle", c.get("title"));
                    hit.put("n", e.get("n")); hit.put("title", e.get("title"));
                    hit.put("grade", e.get("grade")); hit.put("plain", e.get("plain"));
                    hits.add(hit);
                    if (hits.size() >= limit) return hits;
                }
            }
        }
        return hits;
    }

    public long loadedAt() { return loadedAt; }

    // ---- 类型工具（Nashorn 返回 ScriptObjectMirror，均实现 Map/List）----
    static Map<String, Object> asMap(Object o) { @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) o; return m; }
    static List<Object> asList(Object o) {
        if (o == null) return Collections.emptyList();
        if (o instanceof List) {
            @SuppressWarnings("unchecked") List<Object> l = (List<Object>) o;
            return l;
        }
        if (o instanceof jdk.nashorn.api.scripting.ScriptObjectMirror) { // Nashorn 数组
            jdk.nashorn.api.scripting.ScriptObjectMirror m = (jdk.nashorn.api.scripting.ScriptObjectMirror) o;
            if (m.isArray()) {
                @SuppressWarnings("unchecked") List<Object> l = (List<Object>) m.to(List.class);
                return l;
            }
        }
        return Collections.emptyList();
    }
    static int num(Map<String, Object> m) { return ((Number) m.get("num")).intValue(); }
    static String str(Map<String, Object> m, String k) { Object v = m.get(k); return v == null ? "" : v.toString(); }
}
