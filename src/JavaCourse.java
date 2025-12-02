import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaCourse {
    public static void main(String[] args) {
        String path = "C:/Users/vboldyreva/Desktop/AccessLogParser/src/access.log";
        try {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                System.err.println("Файл не существует: " + path);
                return;
            }
            if (!Files.isRegularFile(filePath)) {
                System.err.println("Указанный путь ведёт не к файлу: " + path);
                return;
            }
            try {
                FileReader fileReader = new FileReader(filePath.toFile());
                BufferedReader reader = new BufferedReader(fileReader);
                String line;
                int totalLines = 0;
                int maxLength = 0;
                int minLength = Integer.MAX_VALUE;
                while ((line = reader.readLine()) != null) {
                    totalLines++;
                    int length = line.length();

                    if (length > 1024) {
                        throw new RuntimeException("Найдена строка длиной более 1024 символов: " + length);
                    }
                    if (length > maxLength) {
                        maxLength = length;
                    }
                    if (length < minLength) {
                        minLength = length;
                    }
                }

                System.out.println("Общее количество строк в файле: " + totalLines);
                System.out.println("Длина самой длинной строки в файле: " + maxLength);
                System.out.println("Длина самой короткой строки в файле: " + minLength);
                reader.close();
            } catch (Exception ex) {
               ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при работе с файлом: " + e.getMessage());
            e.printStackTrace();
        }
    }
}





