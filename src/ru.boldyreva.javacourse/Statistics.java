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
    private void parseLine(String line) {
        // Разбиваем по кавычкам
        String[] parts = line.split("\"");
        if (parts.length < 6) return;
        String request = parts[1];   // "GET /page HTTP/1.1"
        String statusPart = parts[2]; // " 200 442 "
        String userAgent = parts[5]; // User-Agent
        // Извлекаем страницу: ищем первый и второй пробел
        int firstSpace = request.indexOf(' ');
        int secondSpace = request.indexOf(' ', firstSpace + 1);
        if (firstSpace == -1 || secondSpace == -1) return;
        String page = request.substring(firstSpace + 1, secondSpace);
        // Извлекаем код ответа
        String statusCode = statusPart.trim().split(" ")[0];
        if ("200".equals(statusCode)) {
            String os = detectOS(userAgent);
            pages.add(page);
            osCount.merge(os, 1, Integer::sum);
        }
    }



    private String detectOS(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "Mac OS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
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











