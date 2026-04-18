/*
Desafio Tecnico Topaz
Desenvolvido por L.A.Leandro
São José dos Campos - SP
18-04-2026
*/
package com.topaz.urlshortener;

import com.topaz.urlshortener.util.Base62;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    
    private static final AtomicLong counter = new AtomicLong(0);
    
    public static void main(String[] args) throws Exception {
        initDatabase();
        
        Server server = new Server(8080);
        
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        
        context.addServlet(IndexServlet.class, "/index.html");
        context.addServlet(IndexServlet.class, "/");
        context.addServlet(ReadmeServlet.class, "/readme");
        context.addServlet(ShortenServlet.class, "/api/shorten");
        context.addServlet(RedirectServlet.class, "/api/*");
        context.addServlet(InfoServlet.class, "/api/info/*");
        context.addServlet(HealthServlet.class, "/api/health");
        
        server.setHandler(context);
        
        System.out.println("========================================");
        System.out.println("URL Shortener");
        System.out.println("  http://localhost:8080/");
        System.out.println("  http://localhost:8080/readme");
        System.out.println("========================================");
        
        server.start();
        server.join();
    }
    
    private static void initDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:urlshortener;DB_CLOSE_DELAY=-1", "sa", "");
        Statement stmt = conn.createStatement();
        
        stmt.execute("CREATE TABLE IF NOT EXISTS url_mappings (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "original_url VARCHAR(2048) NOT NULL," +
            "short_code VARCHAR(10) NOT NULL UNIQUE," +
            "alias VARCHAR(50)," +
            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "access_count BIGINT NOT NULL DEFAULT 0" +
            ")");
        
        stmt.close();
        conn.close();
    }
    
    public static String generateShortCode() throws Exception {
        String shortCode;
        int attempts = 0;
        do {
            long id = counter.incrementAndGet();
            shortCode = Base62.encode(id);
            attempts++;
            if (attempts > 100) throw new RuntimeException("Falha ao gerar codigo unico");
        } while (shortCodeExists(shortCode));
        return shortCode;
    }
    
    public static boolean aliasExists(String alias) throws Exception {
        if (alias == null || alias.isEmpty()) return false;
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM url_mappings WHERE alias = ?");
        ps.setString(1, alias);
        ResultSet rs = ps.executeQuery();
        rs.next();
        boolean exists = rs.getInt(1) > 0;
        rs.close();
        ps.close();
        conn.close();
        return exists;
    }
    
    public static boolean shortCodeExists(String shortCode) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM url_mappings WHERE short_code = ?");
        ps.setString(1, shortCode);
        ResultSet rs = ps.executeQuery();
        rs.next();
        boolean exists = rs.getInt(1) > 0;
        rs.close();
        ps.close();
        conn.close();
        return exists;
    }
    
    public static void saveUrlMapping(String url, String shortCode, String alias) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO url_mappings (original_url, short_code, alias, created_at, access_count) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 0)");
        ps.setString(1, url);
        ps.setString(2, shortCode);
        ps.setString(3, alias);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }
    
    public static String getOriginalUrl(String shortCode) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT original_url FROM url_mappings WHERE short_code = ? OR alias = ?");
        ps.setString(1, shortCode);
        ps.setString(2, shortCode);
        ResultSet rs = ps.executeQuery();
        
        String url = null;
        if (rs.next()) {
            url = rs.getString("original_url");
        }
        
        rs.close();
        ps.close();
        conn.close();
        return url;
    }
    
    public static String[] getUrlInfo(String shortCode) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT original_url, alias, access_count FROM url_mappings WHERE short_code = ? OR alias = ?");
        ps.setString(1, shortCode);
        ps.setString(2, shortCode);
        ResultSet rs = ps.executeQuery();
        
        String[] result = null;
        if (rs.next()) {
            result = new String[3];
            result[0] = rs.getString("original_url");
            result[1] = rs.getString("alias");
            result[2] = rs.getString("access_count");
        }
        
        rs.close();
        ps.close();
        conn.close();
        return result;
    }
    
    public static void incrementAccessCount(String shortCode) throws Exception {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE url_mappings SET access_count = access_count + 1 WHERE short_code = ? OR alias = ?");
        ps.setString(1, shortCode);
        ps.setString(2, shortCode);
        ps.executeUpdate();
        ps.close();
        conn.close();
    }
    
    private static Connection getConnection() throws Exception {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:mem:urlshortener", "sa", "");
    }
    
    public static class ShortenServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
            try {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.addHeader("Access-Control-Allow-Origin", "*");
                resp.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
                
                String url = req.getParameter("url");
                String alias = req.getParameter("alias");
                
                if (url == null || url.trim().isEmpty()) {
                    resp.setStatus(400);
                    resp.getWriter().write("{\"error\":\"URL e obrigatoria\"}");
                    return;
                }
                
                String shortCode;
                if (alias != null && !alias.isEmpty()) {
                    if (alias.length() < 3 || alias.length() > 20 || !alias.matches("^[a-zA-Z0-9]+$")) {
                        resp.setStatus(400);
                        resp.getWriter().write("{\"error\":\"Alias invalido\"}");
                        return;
                    }
                    if (aliasExists(alias)) {
                        resp.setStatus(400);
                        resp.getWriter().write("{\"error\":\"Alias ja esta em uso\"}");
                        return;
                    }
                    shortCode = alias;
                } else {
                    shortCode = generateShortCode();
                }
                
                saveUrlMapping(url, shortCode, alias);
                
                String shortUrl = "http://localhost:8080/" + shortCode;
                resp.setStatus(200);
                resp.getWriter().write("{\"shortUrl\":\"" + shortUrl + "\",\"shortCode\":\"" + shortCode + "\",\"originalUrl\":\"" + url + "\",\"alias\":" + (alias != null ? "\"" + alias + "\"" : "null") + ",\"accessCount\":0}");
            } catch (Exception e) {
                try {
                    resp.setStatus(500);
                    resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
                } catch (Exception ignored) {}
            }
        }
    }
    
    public static class RedirectServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            try {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
                    resp.setContentType("application/json");
                    resp.setStatus(404);
                    resp.getWriter().write("{\"error\":\"Nao encontrado\"}");
                    return;
                }
                
                String shortCode = pathInfo.substring(1);
                String originalUrl = getOriginalUrl(shortCode);
                
                if (originalUrl == null) {
                    resp.setContentType("application/json");
                    resp.setStatus(404);
                    resp.getWriter().write("{\"error\":\"Codigo nao encontrado\"}");
                    return;
                }
                
                incrementAccessCount(shortCode);
                resp.sendRedirect(originalUrl);
            } catch (Exception e) {
                try {
                    resp.setStatus(500);
                    resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
                } catch (Exception ignored) {}
            }
        }
    }
    
    public static class InfoServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            try {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
                    resp.setStatus(404);
                    return;
                }
                
                String shortCode = pathInfo.substring(1);
                String result[] = getUrlInfo(shortCode);
                
                if (result == null) {
                    resp.setStatus(404);
                    resp.getWriter().write("{\"error\":\"Codigo nao encontrado\"}");
                    return;
                }
                
                String shortUrl = "http://localhost:8080/" + shortCode;
                resp.getWriter().write("{\"shortUrl\":\"" + shortUrl + "\",\"shortCode\":\"" + shortCode + "\",\"originalUrl\":\"" + result[0] + "\",\"alias\":" + (result[1] != null ? "\"" + result[1] + "\"" : "null") + ",\"accessCount\":" + result[2] + "}");
            } catch (Exception e) {
                try {
                    resp.setStatus(500);
                    resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
                } catch (Exception ignored) {}
            }
        }
    }
    
    public static class HealthServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            try {
                resp.setContentType("application/json");
                resp.getWriter().write("{\"status\":\"healthy\",\"timestamp\":" + System.currentTimeMillis() + "}");
            } catch (Exception ignored) {}
        }
    }
    
    public static class ReadmeServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            try {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            
            String html = "<!DOCTYPE html>" +
            "<html lang='pt-BR'>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "  <title>URL Shortener</title>" +
            "  <style>" +
            "    * { box-sizing: border-box; margin: 0; padding: 0; }" +
            "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f5; color: #333; line-height: 1.6; }" +
            "    .container { max-width: 600px; margin: 40px auto; padding: 0 20px; }" +
            "    .box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
            "    h1 { color: #333; margin-bottom: 20px; }" +
            "    code { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-family: monospace; }" +
            "    pre { background: #1e1e1e; color: #d4d4d4; padding: 15px; border-radius: 6px; overflow-x: auto; }" +
            "    pre code { background: none; padding: 0; }" +
            "    a { color: #007bff; text-decoration: none; }" +
            "    a:hover { text-decoration: underline; }" +
            "    ul { margin-left: 20px; }" +
            "    li { margin-bottom: 8px; }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class='container'>" +
            "    <div class='box'>" +
            "      <h1>URL Shortener</h1>" +
            "      <p><a href='/'>&larr; Voltar</a></p>" +
            "      <h2>APIs</h2>" +
            "      <ul>" +
            "        <li><code>GET /</code> - Pagina principal</li>" +
            "        <li><code>POST /api/shorten</code> - Encurtar URL</li>" +
            "        <li><code>GET /api/{code}</code> - Redirecionar</li>" +
            "        <li><code>GET /api/info/{code}</code> - Info</li>" +
            "        <li><code>GET /api/health</code> - Health</li>" +
            "      </ul>" +
            "      <h2>Running</h2>" +
            "      <pre><code>mvn clean compile\nmvn jetty:run</code></pre>" +
            "      <p>Access: <code>http://localhost:8080/</code></p>" +
            "    </div>" +
            "  </div>" +
            "</body>" +
            "</html>";
            
            resp.getWriter().write(html);
            } catch (Exception e) {
                try { resp.setStatus(500); resp.getWriter().write(e.getMessage()); } catch (Exception ignored) {}
            }
        }
    }
    
    public static class IndexServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
            try {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            
            String html = "<!DOCTYPE html>" +
            "<html lang='pt-BR'>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "  <title>URL Shortener</title>" +
            "  <style>" +
            "    * { box-sizing: border-box; margin: 0; padding: 0; }" +
            "    html, body { width: 100%; }" +
            "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f5f5; min-height: 100vh; }" +
            "    .header { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); color: white; padding: 40px 20px; text-align: center; }" +
            "    .header h1 { margin-bottom: 10px; font-size: 2rem; }" +
            "    .header a { color: #64b5f6; text-decoration: none; margin: 0 10px; }" +
            "    .header a:hover { text-decoration: underline; }" +
            "    .container { max-width: 500px; margin: 0 auto; padding: 40px 20px; width: 100%; }" +
            "    .form-box { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); width: 100%; }" +
            "    h1 { text-align: center; color: #333; margin-bottom: 30px; }" +
            "    .form-group { margin-bottom: 20px; }" +
            "    label { display: block; margin-bottom: 8px; color: #555; font-weight: 500; }" +
            "    input[type='text'] { width: 100%; padding: 14px; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; transition: border-color 0.3s; }" +
            "    input[type='text']:focus { outline: none; border-color: #007bff; }" +
            "    small { color: #888; font-size: 14px; margin-top: 6px; display: block; }" +
            "    button { width: 100%; padding: 14px; background: #007bff; color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; transition: background 0.3s; }" +
            "    button:hover { background: #0056b3; }" +
            "    button:disabled { background: #ccc; cursor: not-allowed; }" +
            "    .result { margin-top: 24px; padding: 20px; background: #e8f5e9; border-radius: 8px; border-left: 4px solid #28a745; }" +
            "    .result h3 { color: #28a745; margin-bottom: 16px; }" +
            "    .input-group { display: flex; gap: 10px; flex-wrap: wrap; }" +
            "    .input-group input { flex: 1; min-width: 150px; }" +
            "    .copy-btn { padding: 12px 20px; background: #6c757d; color: white; border: none; border-radius: 8px; cursor: pointer; flex-shrink: 0; }" +
            "    .copy-btn:hover { background: #545b62; }" +
            "    .error { margin-top: 20px; padding: 16px; background: #f8d7da; border-radius: 8px; border-left: 4px solid #dc3545; color: #721c24; }" +
            "    .info { margin-top: 12px; color: #666; font-size: 14px; }" +
            "    @media (max-width: 480px) {" +
            "      .header { padding: 30px 15px; }" +
            "      .header h1 { font-size: 1.5rem; }" +
            "      .container { padding: 20px 15px; }" +
            "      .form-box { padding: 20px; }" +
            "      .input-group { flex-direction: column; }" +
            "      .input-group input { width: 100%; }" +
            "      .copy-btn { width: 100%; }" +
            "      input[type='text'] { font-size: 14px; padding: 12px; }" +
            "      button { padding: 12px; font-size: 14px; }" +
            "    }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class='header'>" +
            "    <h1>URL Shortener</h1>" +
            "    <p>Encurtador de URL</p>" +
            "    <div>" +
            "      <a href='/readme'>README</a>" +
            "    </div>" +
            "  </div>" +
            "  <div class='container'>" +
            "    <div class='form-box'>" +
            "      <h1>Encurtar URL</h1>" +
            "      <div class='form-group'>" +
            "        <label for='url'>URL:</label>" +
            "        <input type='text' id='url' placeholder='https://exemplo.com'>" +
            "      </div>" +
            "      <div class='form-group'>" +
            "        <label for='alias'>Alias (opcional):</label>" +
            "        <input type='text' id='alias' placeholder='meu-alias'>" +
            "        <small>Deixe em branco para gerar automaticamente</small>" +
            "      </div>" +
            "      <button id='shortenBtn' onclick='shortenUrl()'>Encurtar URL</button>" +
            "      <div id='result' style='display:none'></div>" +
            "      <div id='error' style='display:none'></div>" +
            "    </div>" +
            "  </div>" +
            "  <script>" +
            "    function shortenUrl() {" +
            "      var urlInput = document.getElementById('url').value;" +
            "      var aliasInput = document.getElementById('alias').value;" +
            "      var btn = document.getElementById('shortenBtn');" +
            "      var resultDiv = document.getElementById('result');" +
            "      var errorDiv = document.getElementById('error');" +
            "      if (!urlInput) { errorDiv.innerHTML = 'Por favor, insira uma URL'; errorDiv.style.display = 'block'; return; }" +
            "      var body = 'url=' + encodeURIComponent(urlInput);" +
            "      if (aliasInput) body = body + '&alias=' + encodeURIComponent(aliasInput);" +
            "      var xhr = new XMLHttpRequest();" +
            "      xhr.open('POST', '/api/shorten', false);" +
            "      xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');" +
            "      try {" +
            "        xhr.send(body);" +
            "        if (xhr.status === 200) {" +
            "          var data = eval('(' + xhr.responseText + ')');" +
            "          if (data.error) { errorDiv.innerHTML = data.error; errorDiv.style.display = 'block'; }" +
            "          else { resultDiv.innerHTML = '<div class=\"result\"><h3>URL Encurtada:</h3><input type=\"text\" id=\"shortUrl\" value=\"' + data.shortUrl + '\" readonly><div class=\"info\">Codigo: ' + data.shortCode + '</div><div class=\"info\">Acessos: ' + data.accessCount + '</div></div>'; resultDiv.style.display = 'block'; }" +
            "        } else { errorDiv.innerHTML = 'Erro ao conectar'; errorDiv.style.display = 'block'; }" +
            "      } catch(e) { errorDiv.innerHTML = 'Erro: ' + e.message; errorDiv.style.display = 'block'; }" +
            "  }" +
            "  </script>" +
            "</body>" +
            "</html>";
            
            resp.getWriter().write(html);
            } catch (Exception e) {
                try { resp.setStatus(500); resp.getWriter().write(e.getMessage()); } catch (Exception ignored) {}
            }
        }
    }
}