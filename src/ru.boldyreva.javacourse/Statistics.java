package ru.boldyreva.javacourse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Statistics {
    private Set<String> pages = new HashSet<>();
    private Map<String, Integer> osCount = new HashMap<>();
    public Statistics(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Файл не найден: " + filePath);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Указанный путь не ведёт к файлу: " + filePath);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseAndAddEntry(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + filePath, e);
        }
    }
    private void parseAndAddEntry(String line) {

        String regex = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/+\\s]+)\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+|-) \"([^\"]*)\" \"([^\"]*)\"$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String page = matcher.group(6);
            String statusCode = matcher.group(8);
            String userAgent = matcher.group(11);

            if ("200".equals(statusCode)) {
                String os = detectOS(userAgent);
                addEntry(page, os);
            }
        }
    }
    private String detectOS(String userAgent) {
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "Mac OS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Unknown";
    }
    private void addEntry(String page, String os) {
        if (page != null && !page.trim().isEmpty()) {
            pages.add(page);
        }
        if (os != null && !os.trim().isEmpty()) {
            osCount.merge(os, 1, Integer::sum);
        }
    }

    public Set<String> getPages() {
        return new HashSet<>(pages);
    }

    public Map<String, Double> getOsStatistics() {
        Map<String, Double> result = new HashMap<>();
        int total = osCount.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return result;
        for (Map.Entry<String, Integer> entry : osCount.entrySet()) {
            result.put(entry.getKey(), (double) entry.getValue() / total);
        }
        return result;
    }

    public static void main(String[] args) {
        Statistics stats = new Statistics("access.log");
        System.out.println("=== Уникальные страницы ===");
        Set<String> pages = stats.getPages();
        System.out.println("Количество: " + pages.size());
        System.out.println("Примеры:");
        pages.stream().limit(5).forEach(System.out::println);
        System.out.println("\n=== Статистика по ОС ===");
        Map<String, Double> osStats = stats.getOsStatistics();
        osStats.forEach((os, share) ->
                System.out.printf("%s: %.2f%%\n", os, share * 100)
        );
    }
}










