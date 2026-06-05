import java.io.*;
import java.nio.file.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String base = args[0];
        Files.walk(Paths.get(base))
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(p -> {
                System.out.println("=== FILE: " + p + " ===");
                try { Files.lines(p).forEach(System.out::println); } catch(Exception e) {}
                System.out.println();
            });
    }
}
