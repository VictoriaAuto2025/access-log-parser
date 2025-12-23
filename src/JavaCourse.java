import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCourse {
    public static void main(String[] args) {
        String logFilePath = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log";
        Statistics stats = new Statistics();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    LogEntry entry = new LogEntry(line);
                    stats.addEntry(entry);
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка при разборе строки: " + line);
                    System.err.println("Причина: " + e.getMessage());
                    continue;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return;
        }
        System.out.println("Обработано записей: " + stats.getEntryCount());
        System.out.println("Средний часовой трафик: " + stats.getTrafficRate() + " байт/час");
    }

    static class LogEntry {
        private final String ipAddr;
        private final LocalDateTime time;
        private final HttpMethod method;
        private final String path;
        private final int responseCode;
        private final int responseSize;
        private final String referer;
        private final UserAgent agent;
        public LogEntry(String logLine) {

            String regex = "^(\\S+) \\S+ \\S+ \\[([^\\]]+)\\] \"([^\"]*)\" (\\d+) (\\d+) \"([^\"]*)\" \"([^\"]*)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(logLine);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Не удалось разобрать строку лога: '" + logLine + "'");
            }
            this.ipAddr = matcher.group(1);
            this.time = parseDate(matcher.group(2));
            this.responseCode = Integer.parseInt(matcher.group(4));
            this.responseSize = Integer.parseInt(matcher.group(5));
            this.referer = matcher.group(6);
            this.agent = new UserAgent(matcher.group(7));

            String request = matcher.group(3);
            String[] parts = request.split(" ", 3);
            if (parts.length < 2) {
                throw new IllegalArgumentException("Некорректный формат запроса: '" + request + "'");
            }
            this.method = HttpMethod.fromString(parts[0]);
            this.path = parts[1];
        }
        private LocalDateTime parseDate(String dateString) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss X", Locale.ENGLISH);
                return LocalDateTime.parse(dateString, formatter);
            } catch (Exception e) {
                throw new IllegalArgumentException("Не удалось разобрать дату: '" + dateString + "'", e);
            }
        }

        public String getIpAddr() { return ipAddr; }
        public LocalDateTime getTime() { return time; }
        public HttpMethod getMethod() { return method; }
        public String getPath() { return path; }
        public int getResponseCode() { return responseCode; }
        public int getResponseSize() { return responseSize; }
        public String getReferer() { return referer; }
        public UserAgent getAgent() { return agent; }
    }


    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH;
        public static HttpMethod fromString(String methodStr) {
            try {
                return valueOf(methodStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    static class UserAgent {
        private final String originalString;
        private final String os;
        private final String browser;
        public UserAgent(String userAgentString) {
            this.originalString = userAgentString;
            this.os = extractOs(userAgentString);
            this.browser = extractBrowser(userAgentString);
        }
        private String extractOs(String ua) {
            if (ua.contains("Windows")) return "Windows";
            if (ua.contains("Mac OS") || ua.contains("macOS")) return "macOS";
            if (ua.contains("Linux")) return "Linux";
            return "Unknown";
        }
        private String extractBrowser(String ua) {
            if (ua.contains("Edge")) return "Edge";
            if (ua.contains("Firefox")) return "Firefox";
            if (ua.contains("Chrome")) return "Chrome";
            if (ua.contains("Opera")) return "Opera";
            if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
            return "Other";
        }
        public String getOriginalString() {
            return originalString;
        }
        public String getOs() {
            return os;
        }
        public String getBrowser() {
            return browser;
        }
    }

    static class Statistics {
        private int totalTraffic = 0;
        private LocalDateTime minTime = null;
        private LocalDateTime maxTime = null;
        private int entryCount = 0; // Для отладки
        public Statistics() {

        }
        public void addEntry(LogEntry entry) {
            totalTraffic += entry.getResponseSize();
            entryCount++;
            LocalDateTime entryTime = entry.getTime();
            if (minTime == null || entryTime.isBefore(minTime)) {
                minTime = entryTime;
            }
            if (maxTime == null || entryTime.isAfter(maxTime)) {
                maxTime = entryTime;
            }
        }
        public double getTrafficRate() {
            if (minTime == null || maxTime == null) {
                return 0.0;
            }
            long seconds = java.time.Duration.between(minTime, maxTime).getSeconds();
            if (seconds == 0) {
                return 0.0;
            }
            double hours = seconds / 3600.0;
            return totalTraffic / hours;
        }
        public int getEntryCount() {
            return entryCount;
        }
    }
}
