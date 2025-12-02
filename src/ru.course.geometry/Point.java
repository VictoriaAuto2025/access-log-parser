package ru.course.geometry;

class Point{
    int x,y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null||getClass() != obj.getClass()) {
            return false;
        }
        final Point other = (Point) obj;
        return this.x == other.x&&this.y == other.y;
    }

    public Point clone() throws CloneNotSupportedException {
        return (Point)super.clone();
    }
}
