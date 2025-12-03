package ru.boldyreva.javacourse;
import java.io.*;
import java.util.*;
public class Statistics {
    private Set<String> pages = new HashSet<>();
    private Map<String, Integer> osCount = new HashMap<>();
    public Statistics(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + filePath, e);
        }
    }
    private boolean parseLine(String line) {
        try {
            // Находим первую кавычку — должна быть перед GET
            int q1 = line.indexOf('"');
            if (q1 == -1) return false;
            // Проверяем, что после кавычки идёт GET/POST/HEAD
            if (!line.startsWith("GET ", q1 + 1) &&
                    !line.startsWith("POST ", q1 + 1) &&
                    !line.startsWith("HEAD ", q1 + 1)) {
                return false;
            }
            // Находим вторую кавычку — после запроса
            int q2 = line.indexOf('"', q1 + 1);
            if (q2 == -1) return false;
            String request = line.substring(q1 + 1, q2); // "GET /page HTTP/1.1"
            // Извлекаем страницу
            int space1 = request.indexOf(' ');
            if (space1 == -1) return false;
            int space2 = request.indexOf(' ', space1 + 1);
            if (space2 == -1) return false;
            String page = request.substring(space1 + 1, space2);
            // Код ответа — после второй кавычкой
            String afterQ2 = line.substring(q2 + 1).trim();
            String statusCode = afterQ2.split("\\s+")[0];
            if (!"200".equals(statusCode)) return false;
            // User-Agent — между последними двумя кавычками
            int lastQ = line.lastIndexOf('"');
            int prevQ = line.lastIndexOf('"', lastQ - 1);
            if (prevQ == -1) return false;
            String userAgent = line.substring(prevQ + 1, lastQ);
            if ("-".equals(userAgent.trim())) {
                return true;
            }
            String os = detectOS(userAgent);
            pages.add(page);
            osCount.merge(os, 1, Integer::sum);
            return true;
        } catch (Exception e) {
            return false;
        }
    }










    private String detectOS(String ua) {
        if (ua == null) return "Unknown";
        // Приводим к нижнему регистру
        String lowerUa = ua.toLowerCase();
        // Проверяем по ключевым словам
        if (lowerUa.contains("windows")) return "Windows";
        if (lowerUa.contains("mac")) return "Mac OS";
        if (lowerUa.contains("linux")) return "Linux";
        if (lowerUa.contains("android")) return "Android";
        if (lowerUa.contains("iphone") || lowerUa.contains("ipad")) return "iOS";
        // Дополнительные проверки
        if (lowerUa.contains("ubuntu")) return "Linux";
        if (lowerUa.contains("centos")) return "Linux";
        if (lowerUa.contains("fedora")) return "Linux";
        if (lowerUa.contains("debian")) return "Linux";
        if (lowerUa.contains("chrome os")) return "Chrome OS";
        // Если ничего не подошло — Unknown
        return "Unknown";
    }

    public Set<String> getPages() {
        return new HashSet<>(pages);
    }
    public Map<String, Double> getOsStatistics() {
        Map<String, Double> result = new HashMap<>();
        int total = osCount.values().stream().mapToInt(Integer::intValue).sum();
        if (total > 0) {
            for (String os : osCount.keySet()) {
                result.put(os, (double) osCount.get(os) / total);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Statistics stats = new Statistics("C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log");

        System.out.println("Страницы (" + stats.getPages().size() + "):");
        stats.getPages().forEach(System.out::println);
        System.out.println("\nОС:");
        stats.getOsStatistics().forEach((os, share) ->
                System.out.printf("%s: %.1f%%%n", os, share * 100)
        );
    }
}












