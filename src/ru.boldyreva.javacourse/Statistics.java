import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Statistics {

    private final Map<Long, Integer> visitsPerSecond = new HashMap<>();
    private final Set<String> refererDomains = new HashSet<>();
    private final Map<String, Integer> userVisits = new HashMap<>();
    public void addEntry(String logLine) {
        LogEntry entry = parseLogLine(logLine);
        if (entry == null || entry.getDateTime() == null) return;

        if (isBot(entry.getUserAgent())) {
            return;
        }

        long second = entry.getDateTime().toEpochSecond(java.time.ZoneOffset.UTC);
        visitsPerSecond.merge(second, 1, Integer::sum);

        String referer = entry.getReferer();
        if (referer != null && !referer.equals("-") && !referer.isEmpty()) {
            String domain = extractDomain(referer);
            if (domain != null) {
                refererDomains.add(domain);
            }
        }

        userVisits.merge(entry.getIpAddress(), 1, Integer::sum);
    }

    public int calculatePeakVisitsPerSecond() {
        return visitsPerSecond.isEmpty() ? 0 : Collections.max(visitsPerSecond.values());
    }

    public Set<String> getRefererDomains() {
        return new HashSet<>(refererDomains); // возвращаем копию
    }

    public int calculateMaxVisitsPerUser() {
        return userVisits.isEmpty() ? 0 : Collections.max(userVisits.values());
    }

    private boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.equals("-") || userAgent.isEmpty()) return true;
        String ua = userAgent.toLowerCase();
        return ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")
                || ua.startsWith("curl/") || ua.startsWith("wget/");
    }

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

    private LogEntry parseLogLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        try {
            String[] parts = line.split("\"");
            if (parts.length < 4) return null;

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

    public static void main(String[] args) {
        String logFilePath = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log";
        Statistics stats = new Statistics();
        try (Scanner sc = new Scanner(new java.io.File(logFilePath))) {
            while (sc.hasNextLine()) {
                stats.addEntry(sc.nextLine());
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        System.out.println("Пиковая посещаемость в секунду: " + stats.calculatePeakVisitsPerSecond());
        System.out.println("Количество рефереров: " + stats.getRefererDomains().size());
        System.out.println("Рефереры: " + stats.getRefererDomains());
        System.out.println("Макс. посещений одним пользователем: " + stats.calculateMaxVisitsPerUser());
    }
}
