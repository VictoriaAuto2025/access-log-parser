import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class Statistics {
    // Поля, необходимые по заданию
    private final Map<Long, Integer> visitsPerSecond = new HashMap<>();       // для пика в секунду
    private final Set<String> refererDomains = new HashSet<>();               // для списка рефереров
    private final Map<String, Integer> userVisits = new HashMap<>();          // для макс. посещаемости пользователем
    public void addEntry(String logLine) {
        LogEntry entry = parseLogLine(logLine);
        if (entry == null || entry.getDateTime() == null) return;
        // Пропускаем ботов (определяем по User-Agent)
        if (isBot(entry.getUserAgent())) {
            return;
        }
        // 1. Считаем посещения по секундам
        long second = entry.getDateTime().toEpochSecond(java.time.ZoneOffset.UTC);
        visitsPerSecond.merge(second, 1, Integer::sum);
        // 2. Сохраняем домен из Referer (если есть)
        String referer = entry.getReferer();
        if (referer != null && !referer.equals("-") && !referer.isEmpty()) {
            String domain = extractDomain(referer);
            if (domain != null) {
                refererDomains.add(domain);
            }
        }
        // 3. Считаем посещения по IP
        userVisits.merge(entry.getIpAddress(), 1, Integer::sum);
    }
    // Метод 1: пиковая посещаемость в секунду
    public int calculatePeakVisitsPerSecond() {
        return visitsPerSecond.isEmpty() ? 0 : Collections.max(visitsPerSecond.values());
    }
    // Метод 2: список рефереров (доменов)
    public Set<String> getRefererDomains() {
        return new HashSet<>(refererDomains); // возвращаем копию
    }
    // Метод 3: максимальная посещаемость одним пользователем
    public int calculateMaxVisitsPerUser() {
        return userVisits.isEmpty() ? 0 : Collections.max(userVisits.values());
    }
    // Простая проверка на бота (без отдельного класса)
    private boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.equals("-") || userAgent.isEmpty()) return true;
        String ua = userAgent.toLowerCase();
        return ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")
                || ua.startsWith("curl/") || ua.startsWith("wget/");
    }
    // Извлечение домена из URL
    private String extractDomain(String url) {
        try {
            if (url.startsWith("http://")) url = url.substring(7);
            else if (url.startsWith("https://")) url = url.substring(8);
            int end = url.indexOf('/');
            if (end == -1) end = url.indexOf('?');
            if (end == -1) end = url.length();
            String domain = url.substring(0, end).split(":")[0]; // убираем порт
            return domain.isEmpty() ? null : domain.toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }
    // Простейший парсер лога (под формат Apache)
    private LogEntry parseLogLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        try {
            String[] parts = line.split("\"");
            if (parts.length < 4) return null;
            // IP и дата — до первой кавычки
            String preQuote = parts[0].trim();
            String[] pre = preQuote.split("\\s+");
            if (pre.length < 4) return null;
            String ip = pre[0];
            String dateStr = parts[0].substring(parts[0].indexOf('[') + 1, parts[0].indexOf(']')).split(" ")[0];
            LocalDateTime dt = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH));
            int status = Integer.parseInt(parts[2].trim().split("\\s+")[0]);
            String referer = parts.length > 3 ? parts[3].trim() : "-";
            String userAgent = parts.length > 5 ? parts[5].trim() : "-";
            return new LogEntry(dt, ip, status, referer, userAgent);
        } catch (Exception e) {
            return null;
        }
    }
    // Минимальный класс записи
    private static class LogEntry {
        private final LocalDateTime dateTime;
        private final String ipAddress;
        private final int statusCode;
        private final String referer;
        private final String userAgent;
        LogEntry(LocalDateTime dateTime, String ipAddress, int statusCode, String referer, String userAgent) {
            this.dateTime = dateTime;
            this.ipAddress = ipAddress;
            this.statusCode = statusCode;
            this.referer = referer;
            this.userAgent = userAgent;
        }
        public LocalDateTime getDateTime() { return dateTime; }
        public String getIpAddress() { return ipAddress; }
        public String getReferer() { return referer; }
        public String getUserAgent() { return userAgent; }
    }
    // === MAIN с жёстко заданным путём ===
    public static void main(String[] args) {
        String logFilePath = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log"; // ← замените на ваш путь
        Statistics stats = new Statistics();
        try (Scanner sc = new Scanner(new java.io.File(logFilePath))) {
            while (sc.hasNextLine()) {
                stats.addEntry(sc.nextLine());
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }
        // Вывод результатов
        System.out.println("Пиковая посещаемость в секунду: " + stats.calculatePeakVisitsPerSecond());
        System.out.println("Количество рефереров: " + stats.getRefererDomains().size());
        System.out.println("Рефереры: " + stats.getRefererDomains());
        System.out.println("Макс. посещений одним пользователем: " + stats.calculateMaxVisitsPerUser());
    }
}
