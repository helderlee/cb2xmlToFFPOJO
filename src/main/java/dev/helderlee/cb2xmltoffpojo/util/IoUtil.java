package dev.helderlee.cb2xmltoffpojo.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class IoUtil {
    
    public static void checkDirectory(String directory) {
        File directoryFolder = getDirectory(directory);
        if (!directoryFolder.isDirectory()) {
            Object[] params = new Object[]{directory};
            System.out.println(MessageFormat.format("Directory ''{0}'' not found.", params));
            System.exit(1);
        }
    }
    
    public static File getDirectory(String directory) {
        String currentDir = System.getProperty("user.dir");
        Path currentPath = Paths.get(currentDir);
        Path directoryPath = Paths.get(currentPath.toString(), directory);
        return directoryPath.toFile();
    }
    
}
