package com.rszx;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 人生哲学 · 纯离线阅读 App 后端（零依赖，JDK8+ com.sun.net.httpserver）
 *
 * API（全部 GET）：
 *   /api/meta                书目元信息
 *   /api/chapters            全部章节（不含条目正文）
 *   /api/chapters/{num}      单章（含条目）
 *   /api/entries/{ch}/{n}    单条
 *   /api/search?q=...        全文检索（limit=50）
 *   /api/health              健康检查
 *   /*                       静态托管 frontend/dist（SPA 回退 index.html）
 *
 * 用法：java com.rszx.Server [port] [bookJsonPath] [staticDir]
 */
public final class Server {

    private static final int DEFAULT_PORT = 8080;
    private final Book book;
    private final Path staticDir;

    private Server(Book book, Path staticDir) {
        this.book = book;
        this.staticDir = staticDir;
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        String host = args.length > 3 ? args[3] : "127.0.0.1"; // P2-1：默认仅本机，需局域网显式传 0.0.0.0
        Path bookJson = Paths.get(args.length > 1 ? args[1] : "data/book.json");
        Path staticDir = Paths.get(args.length > 2 ? args[2] : "../frontend/dist");

        Book book = Book.load(bookJson);
        System.out.println("[rszx] 已加载 book.json：章节=" + book.chapters().size()
                + " 版本=" + Book.str(book.meta(), "version"));

        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));
        Server app = new Server(book, staticDir);
        server.createContext("/", app::handle);
        server.start();
        System.out.println("[rszx] 服务已启动 http://" + host + ":" + port
                + "  静态目录=" + staticDir.toAbsolutePath());
    }

    private void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String query = ex.getRequestURI().getRawQuery();
            if (path.startsWith("/api/")) {
                routeApi(ex, path, parseQuery(query));
            } else {
                serveStatic(ex, path);
            }
        } catch (Exception e) {
            System.out.println("[rszx] 500 " + ex.getRequestURI() + " : " + e); // P1-6：细节只进日志
            sendJson(ex, 500, error("服务器内部错误"));
        } finally {
            ex.close();
        }
    }

    private void routeApi(HttpExchange ex, String path, Map<String, String> q) throws IOException {
        String method = ex.getRequestMethod();
        if (!"GET".equals(method)) {
            sendJson(ex, 405, error("仅支持 GET"));
            return;
        }
        switch (path) {
            case "/api/health":
                sendJson(ex, 200, map("ok", true, "loadedAt", book.loadedAt()));
                return;
            case "/api/meta":
                sendJson(ex, 200, book.meta());
                return;
            case "/api/chapters":
                sendJson(ex, 200, map("chapters", book.chapters()));
                return;
            case "/api/search": {
                String limitRaw = q.getOrDefault("limit", "50");
                int limit = Math.max(1, Math.min(200, parseIntSafe(limitRaw, 50)));
                sendJson(ex, 200, map("q", q.getOrDefault("q", ""), "hits", book.search(q.get("q"), limit)));
                return;
            }
            default:
                break;
        }
        // /api/chapters/{num} 与 /api/entries/{ch}/{n}
        String[] seg = path.split("/");
        if (seg.length == 4 && seg[2].equals("chapters")) {
            Integer num = parseIntOrNull(seg[3]);
            Map<String, Object> c = num == null ? null : book.chapter(num);
            if (c == null) { sendJson(ex, 404, error("章节不存在: " + seg[3])); return; }
            sendJson(ex, 200, c);
            return;
        }
        if (seg.length == 5 && seg[2].equals("entries")) {
            Integer ch = parseIntOrNull(seg[3]);
            Integer n = parseIntOrNull(seg[4]);
            Map<String, Object> e = (ch == null || n == null) ? null : book.entry(ch, n);
            if (e == null) { sendJson(ex, 404, error("条目不存在: " + seg[3] + "/" + seg[4])); return; }
            sendJson(ex, 200, e);
            return;
        }
        sendJson(ex, 404, error("未知 API: " + path));
    }

    // ---- 静态文件（带 SPA 回退）----
    private void serveStatic(HttpExchange ex, String path) throws IOException {
        if (!Files.isDirectory(staticDir)) {
            sendJson(ex, 503, error("静态目录不存在: " + staticDir + "（请先构建前端: npm run build）"));
            return;
        }
        String rel = path.equals("/") ? "index.html" : path.substring(1);
        Path file = staticDir.resolve(rel).normalize();
        if (!file.startsWith(staticDir.normalize())) { // 防目录穿越
            sendJson(ex, 403, error("Forbidden"));
            return;
        }
        if (!Files.isRegularFile(file)) {
            // P2-3：仅无扩展名路径（SPA 路由）回退 index.html；资源请求直接 404
            if (rel.contains(".")) { sendJson(ex, 404, error("Not Found")); return; }
            file = staticDir.resolve("index.html");
        }
        if (!Files.isRegularFile(file)) {
            sendJson(ex, 404, error("Not Found"));
            return;
        }
        byte[] body = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", mime(file.getFileName().toString()));
        applySecurityHeaders(ex); // P1-5
        ex.getResponseHeaders().set("Cache-Control", rel.startsWith("assets/") ? "max-age=31536000, immutable" : "no-cache");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }

    private static String mime(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html")) return "text/html; charset=utf-8";
        if (n.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (n.endsWith(".css")) return "text/css; charset=utf-8";
        if (n.endsWith(".json") || n.endsWith(".webmanifest")) return "application/json; charset=utf-8";
        if (n.endsWith(".svg")) return "image/svg+xml";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".ico")) return "image/x-icon";
        if (n.endsWith(".woff2")) return "font/woff2";
        return "application/octet-stream";
    }

    // ---- 工具 ----
    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> q = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return q;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            q.put(decodeUtf8(k), decodeUtf8(v));
        }
        return q;
    }

    private static String decodeUtf8(String s) {
        try { return URLDecoder.decode(s, "UTF-8"); }
        catch (java.io.UnsupportedEncodingException e) { return s; } // UTF-8 恒可用
    }

    private static Integer parseIntOrNull(String s) {
        try { return Integer.valueOf(s); } catch (Exception e) { return null; }
    }

    private static int parseIntSafe(String s, int dft) {
        try { return Integer.parseInt(s); } catch (Exception e) { return dft; }
    }

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private static Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", msg);
        return m;
    }

    /** P1-5：统一基础安全响应头 */
    private static void applySecurityHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        ex.getResponseHeaders().set("X-Frame-Options", "DENY");
        ex.getResponseHeaders().set("Referrer-Policy", "no-referrer");
    }

    private static void sendJson(HttpExchange ex, int status, Object obj) throws IOException {
        byte[] body = Json.write(obj).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        applySecurityHeaders(ex);
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }
}
