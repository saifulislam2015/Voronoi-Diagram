import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        try {
            Voronoi v = new Voronoi();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
