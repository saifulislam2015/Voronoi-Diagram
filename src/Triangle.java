public class Triangle {
    Point a;
    Point b;
    Point c;
    Point center;
    public Triangle(Point a, Point b, Point c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    @Override
    public String toString() {
        String s = a + " " + b + " " + c;
        return s;
    }

    public void setCenter(Point p) {
        this.center = p;
    }

    public Point getCenter() {
        return center;
    }

}
