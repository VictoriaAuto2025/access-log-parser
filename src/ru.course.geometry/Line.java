package ru.course.geometry;

import java.util.Objects;

class Line{
    Point start,end;
    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }


    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null||getClass() != obj.getClass()) {
            return false;
        }
        final Line other = (Line) obj;
        if (!Objects.equals(this.start, other.start)) {
            return false;
        }
        if (!Objects.equals(this.end, other.end)) {
            return false;
        }
        return true;
    }

    public Line clone() throws CloneNotSupportedException {
        Line line= (Line)super.clone();
        line.start=start.clone();
        line.end=end.clone();
        return line;
    }
}