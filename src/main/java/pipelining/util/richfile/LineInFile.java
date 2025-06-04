package pipelining.util.richfile;

import java.nio.file.Path;

public class LineInFile {
    public String line;
    public Path path;
    public int lineNumber;

    public LineInFile(String line, Path path, int lineNumber) {
        this.line = line;
        this.path = path;
        this.lineNumber = lineNumber;
    }

    public LineInFile changeString(String newLine) {
        return new LineInFile(newLine, path, lineNumber);
    }

}
