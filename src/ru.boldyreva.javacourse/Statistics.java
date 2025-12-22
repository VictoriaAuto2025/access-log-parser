import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Statistics {
    private final Map<String, Long> visitsPerHour = new HashMap<>();
    private final Map<String, Long> errorRequestsPerHour = new HashMap<>();
    private long totalVisitsByRealUsers = 0;
    private final Set<String> uniqueUserIPs = new HashSet<>();
    public void addEntry(String logLine) {
        LogEntry entry = parseLogLine(logLine);
        if (entry == null || entry.getDateTime() == null) {
            return;
        }
        boolean isBot = entry.getUserAgent().isBot();
        int statusCode = entry.getStatusCode();
        boolean isError = (statusCode >= 400 && statusCode < 600);
        if (!isBot) {
            totalVisitsByRealUsers++;
            uniqueUserIPs.add(entry.getIpAddress());
            String hourKey = entry.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
            visitsPerHour.merge(hourKey, 1L, Long::sum);
        }
        if (isError) {
            String hourKey = entry.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
            errorRequestsPerHour.merge(hourKey, 1L, Long::sum);
        }
    }
    public double calculateAverageVisitsPerHour() {
        if (visitsPerHour.isEmpty()) return 0.0;
        long total = visitsPerHour.values().stream().mapToLong(Long::longValue).sum();
        return (double) total / visitsPerHour.size();
    }
    public double calculateAverageErrorRequestsPerHour() {
        if (errorRequestsPerHour.isEmpty()) return 0.0;
        long total = errorRequestsPerHour.values().stream().mapToLong(Long::longValue).sum();
        return (double) total / errorRequestsPerHour.size();
    }
    public double calculateAverageVisitsPerUser() {
        if (uniqueUserIPs.isEmpty()) return 0.0;
        return (double) totalVisitsByRealUsers / uniqueUserIPs.size();
    }
    private LogEntry parseLogLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        try {
            int firstSpace = line.indexOf(' ');
            if (firstSpace == -1) return null;
            String ip = line.substring(0, firstSpace);
            int startBracket = line.indexOf('[');
            int endBracket = line.indexOf(']');
            if (startBracket == -1 || endBracket == -1) return null;
            String datetimeStr = line.substring(startBracket + 1, endBracket).split(" ")[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);
            LocalDateTime dateTime = LocalDateTime.parse(datetimeStr, formatter);
            String[] quotedParts = line.split("\"");
            if (quotedParts.length < 6) return null;
            String statusLine = quotedParts[2].trim();
            String[] statusTokens = statusLine.split("\\s+");
            if (statusTokens.length < 1) return null;
            int statusCode = Integer.parseInt(statusTokens[0]);
            String userAgentRaw = quotedParts.length >= 6 ? quotedParts[5].trim() : "";
            return new LogEntry(dateTime, ip, userAgentRaw, statusCode);
        } catch (Exception e) {
            return null;
        }
    }

    public static class UserAgent {
        private final String raw;
        public UserAgent(String raw) {
            this.raw = (raw == null) ? "" : raw.trim();
        }
        public boolean isBot() {
            String s = raw.toLowerCase();
            return s.contains("bot") ||
                    s.contains("crawler") ||
                    s.contains("spider") ||
                    s.startsWith("curl/") ||
                    s.startsWith("wget/") ||
                    s.equals("-") ||
                    s.isEmpty();
        }
        @Override
        public String toString() {
            return raw;
        }
    }
    public static class LogEntry {
        private final LocalDateTime dateTime;
        private final String ipAddress;
        private final UserAgent userAgent;
        private final int statusCode;
        public LogEntry(LocalDateTime dateTime, String ipAddress, String userAgentRaw, int statusCode) {
            this.dateTime = dateTime;
            this.ipAddress = ipAddress;
            this.userAgent = new UserAgent(userAgentRaw);
            this.statusCode = statusCode;
        }
        public LocalDateTime getDateTime() { return dateTime; }
        public String getIpAddress() { return ipAddress; }
        public UserAgent getUserAgent() { return userAgent; }
        public int getStatusCode() { return statusCode; }
    }

    public static void main(String[] args) {

        String logFilePath = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log";
        Statistics stats = new Statistics();
        try (Scanner scanner = new Scanner(new java.io.File(logFilePath))) {
            while (scanner.hasNextLine()) {
                stats.addEntry(scanner.nextLine());
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Файл не найден: " + logFilePath);
            System.exit(1);
        }

        System.out.printf("Среднее число посещений за час (реальные пользователи): %.2f%n", stats.calculateAverageVisitsPerHour());
        System.out.printf("Среднее число ошибочных запросов в час: %.2f%n", stats.calculateAverageErrorRequestsPerHour());
        System.out.printf("Средняя посещаемость одним пользователем: %.2f%n", stats.calculateAverageVisitsPerUser());
    }
}
