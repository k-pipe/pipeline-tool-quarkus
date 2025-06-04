package pipelining.util;

import pipelining.application.Application;
import pipelining.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileUtil {

    private static final CharSequence YAML_SEPARATOR = "\n---\n";

    public static void createParentFolderIfNotExists(Path path) {
        Path folder = path.getParent();
        if (!folder.toFile().exists()) {
            Log.log("Creating non existant folder "+folder);
            Expect.isTrue(folder.toFile().mkdirs()).elseFail("Could not create folder "+folder);
        }
    }

    public static void deleteFileIfExists(Path path) {
        if (path.toFile().exists()) {
            Log.log("Deleting existing file "+path);
            Expect.isTrue(path.toFile().delete()).elseFail("Could not delete file "+path);
        }
    }

    public static void appendYamlSeparator(Path output) {
        try {
            Files.writeString(output, YAML_SEPARATOR, StandardOpenOption.APPEND);
        } catch (IOException e) {
            Application.fail("Could not write to file "+output+", exception occurred: "+e);
        }
    }
}
