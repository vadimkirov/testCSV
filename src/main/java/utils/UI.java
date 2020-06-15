package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class UI {

    public static List<Path> filesList (final String filters, File directory) throws IOException {
        // обход папок в глубину
        return  Files.walk(Paths.get(String.valueOf(directory)))
                .filter(Files::isRegularFile)
                .filter(f-> f.toString().endsWith(filters))
                .collect(Collectors.toList());
    }

}
