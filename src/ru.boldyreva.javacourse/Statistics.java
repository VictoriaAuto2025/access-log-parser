import java.io.*;
import java.util.*;
public class Statistics {
    private final Set<String> notFoundPages = new HashSet<>();
    private final Map<String, Integer> browserCount = new HashMap<>();

    public Statistics(String logFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка: не удалось прочитать файл '" + logFilePath + "'");
            System.err.println("Убедитесь, что файл существует и путь указан верно.");
            System.exit(1);
        }
    }

    private void parseLine(String line) {
        try {
            int q1 = line.indexOf('"');
            int q2 = line.indexOf('"', q1 + 1);
            if (q1 == -1 || q2 == -1) return;
            String request = line.substring(q1 + 1, q2);
            String[] parts = request.split("\\s+");
            if (parts.length < 2) return;
            String page = parts[1];
            String afterQ2 = line.substring(q2 + 1).trim();
            String statusCode = afterQ2.split("\\s+", 2)[0];
            int lastQ = line.lastIndexOf('"');
            int prevQ = line.lastIndexOf('"', lastQ - 1);
            if (prevQ == -1) return;
            String userAgent = line.substring(prevQ + 1, lastQ).trim();
            if ("404".equals(statusCode)) {
                notFoundPages.add(page);
            }
            if (isBot(userAgent)) {
                return;
            }
            String browser = detectBrowser(userAgent);
            browserCount.merge(browser, 1, Integer::sum);
        } catch (Exception ignored) { }
    }
    private boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.isEmpty() || "-".equals(userAgent)) {
            return false;
        }
        String ua = userAgent.toLowerCase();
        return ua.contains("bot") || ua.contains("spider") || ua.contains("crawl") ||
                ua.contains("yandex") || ua.contains("google") || ua.contains("bing") ||
                ua.contains("duckduckgo") || ua.contains("facebook") || ua.contains("whatsapp");
    }
    private String detectBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg")) return "Edge";
        if (ua.contains("chrome") && !ua.contains("chromium")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Safari";
        if (ua.contains("opera") || ua.contains("opr/")) return "Opera";
        if (ua.contains("msie") || ua.contains("trident")) return "Internet Explorer";
        return "Other";
    }

    public Set<String> getNotFoundPages() {
        return new HashSet<>(notFoundPages);
    }
    public Map<String, Double> getBrowserStatistics() {
        int total = browserCount.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> result = new HashMap<>();
        if (total == 0) return result;
        for (Map.Entry<String, Integer> entry : browserCount.entrySet()) {
            result.put(entry.getKey(), (double) entry.getValue() / total);
        }
        return result;
    }

    public static void main(String[] args) {
               String logFile = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log";
               Statistics stats = new Statistics(logFile);
        System.out.println("=== Несуществующие страницы (404): ===");
        if (stats.getNotFoundPages().isEmpty()) {
            System.out.println("Нет несуществующих страниц.");
        } else {
            stats.getNotFoundPages().forEach(System.out::println);
        }
        System.out.println("\n=== Статистика браузеров пользователей: ===");
        Map<String, Double> browserStats = stats.getBrowserStatistics();
        if (browserStats.isEmpty()) {
            System.out.println("Нет данных о браузерах.");
        } else {
            browserStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(entry ->
                            System.out.printf("%s: %.4f%n", entry.getKey(), entry.getValue())
                    );
        }
    }
}
