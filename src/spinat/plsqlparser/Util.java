package spinat.plsqlparser;


import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Util {
    
    public static String loadFile(String filename) {
        Path p = FileSystems.getDefault().getPath(filename);
        try {
            byte [] b = java.nio.file.Files.readAllBytes(p);
            return new String(b,StandardCharsets.ISO_8859_1);
        } catch (Exception ex) {
           throw new RuntimeException("wrapped",ex);
        }
    }
}
