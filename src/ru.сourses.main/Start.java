package ru.сourses.main.Start;

public class Start {
    public static void main(String[] args) {
        int var1=lastNumSum(5,11);
        var1=lastNumSum(var1,123);
        var1=lastNumSum(var1,14);
        var1=lastNumSum(var1,1);
        System.out.println(var1);
    }
    public static int lastNumSum(int a, int b){
        return (a%10)+(b%10);
    }
}
