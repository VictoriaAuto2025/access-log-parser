import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaCourse {

        public static void main(String[] args) {

            String path = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log";
            int totalRequests = 0;
            int yandexBotCount = 0;
            int googleBotCount = 0;

                Path filePath = Paths.get(path);
                if (!Files.exists(filePath)) {
                    System.err.println("Файл не существует: " + path);
                    return;
                }
                if (!Files.isRegularFile(filePath)) {
                    System.err.println("Указанный путь ведёт не к файлу: " + path);
                    return;
                }
            try  {
                FileReader fileReader = new FileReader(filePath.toFile());
                BufferedReader reader = new BufferedReader(fileReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    totalRequests++;

                    String userAgent = extractUserAgent(line);
                    if (userAgent == null) {
                        continue;
                    }

                    String botName = processUserAgent(userAgent);
                    if ("YandexBot".equals(botName)) {
                        yandexBotCount++;
                    } else if ("GoogleBot".equals(botName)) {
                        googleBotCount++;
                    }
                }

                double yandexRatio = totalRequests > 0 ? (double) yandexBotCount / totalRequests : 0.0;
                double googleRatio = totalRequests > 0 ? (double) googleBotCount / totalRequests : 0.0;
                System.out.printf("Доля запросов от YandexBot: %.4f%n", yandexRatio);
                System.out.printf("Доля запросов от GoogleBot: %.4f%n", googleRatio);
            } catch (FileNotFoundException e) {
                System.err.println("Файл '" + filePath + "' не найден");
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла: " + e.getMessage());
            }
        }

        private static String extractUserAgent(String logLine) {
            int lastQuote = logLine.lastIndexOf('"');
            if (lastQuote == -1) return null;
            int prevQuote = logLine.lastIndexOf('"', lastQuote - 1);
            if (prevQuote == -1) return null;
            return logLine.substring(prevQuote + 1, lastQuote);
        }

        private static String processUserAgent(String userAgent) {

            int openBracketIndex = userAgent.indexOf('(');
            int closeBracketIndex = userAgent.indexOf(')', openBracketIndex);
            if (openBracketIndex == -1 || closeBracketIndex == -1) {
                return null;
            }
            String firstBrackets = userAgent.substring(openBracketIndex + 1, closeBracketIndex);

            String[] parts = firstBrackets.split(";");
            if (parts.length >= 2) {
                String fragment = parts[1];

                fragment = fragment.trim();

                int slashIndex = fragment.indexOf('/');
                if (slashIndex == -1) {
                    return null;
                }
                String botName = fragment.substring(0, slashIndex).trim();

                if ("YandexBot".equals(botName) || "GoogleBot".equals(botName)) {
                    return botName;
                }
            }
            return null;
        }
    }
