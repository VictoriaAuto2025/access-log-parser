package ru.boldyreva.javacourse;

class Souce{
    String name;
    Hotness hot;

    public Souce(String name, Hotness hot) {
        this.name = name;
        this.hot = hot;
    }

    public String toString() {
        return "Souce{" +  name + ":" + hot + '}';
    }
}
enum Hotness{VERY_HOT,HOT,NOT_HOT}
