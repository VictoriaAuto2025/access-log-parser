import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Введите первое число:");
        int firstnumber = new Scanner(System.in).nextInt();
        System.out.println("Введите второе число:");
        int secondNumber = new Scanner(System.in).nextInt();
        System.out.println(("Сумма: ") + (firstnumber + secondNumber));
        System.out.println(("Разность: ") + (firstnumber - secondNumber));
        System.out.println(("Произведение: ") + (firstnumber * secondNumber));
        System.out.println(("Частное: ") + (double)firstnumber/secondNumber);
    }
}
