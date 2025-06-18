package pipelining.util;

import pipelining.application.Application;
import pipelining.logging.Log;
import pipelining.script.pipeline.localrunner.PipelineRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileCache {

    private final static String CACHE_DIR = (Application.isRunningInDocker() ? PipelineRunner.WORKDIR : ".") + "/.cache";

    public static Path cachePath(String filename) {
        return Path.of(CACHE_DIR, filename);
    }

    public static File cacheFile(String filename) {
        return cachePath(filename).toFile();
    }

    public static boolean exists(String filename) {
        return cacheFile(filename).exists();
    }

    public static void ensureCacheFolderExists() {
        File folder = Path.of(CACHE_DIR).toFile();
        if (!folder.exists()) {
            Log.log("Creating cache folder: "+folder);
            folder.mkdirs();
        }
    }

    public static List<String> read(String filename) {
        try {
            return Files.readAllLines(cachePath(filename));
        } catch (IOException e) {
            Application.fail("Could not read cache file: "+e);
            return null;
        }
    }

    public static void write(String filename, List<String> lines) {
        ensureCacheFolderExists();
        try {
            Files.writeString(cachePath(filename), lines.stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            Application.fail("Could not read cache file: "+e);
        }
    }

    public static void clean() {
        int count = 0;
        try (Stream<Path> files = Files.list(Path.of(CACHE_DIR))) {
            for (Path file : files.collect(Collectors.toList())) {
                Files.delete(file);
                count++;
            }
        } catch (IOException e) {
            Application.fail("could not clean file cache");
        }
        Log.log("Removed "+count+" files in file cache");
    }
}
