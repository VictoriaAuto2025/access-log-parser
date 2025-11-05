import java.io.File;

import java.util.Scanner;


public class JavaCourse {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int fileCounter = 0;
        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = new Scanner(System.in).nextLine();
            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isDirectory = file.isDirectory();
            if (!fileExists || isDirectory) {
                System.out.println("Файл не существует или указанный путь является путем к папке, а не к файлу");
                continue;
            }
            fileCounter++;
            System.out.println("Путь указан верно");
            System.out.println("Это файл номер " + fileCounter);
        }
    }

}





