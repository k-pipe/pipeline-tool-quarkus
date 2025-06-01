package com.kneissler.util.richfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.kneissler.util.richfile.Constants.COMMENT;

public class ResolveUtil {

    public static int determineIndent(String line) {
        int i = 0;
        while ((i < line.length()) && (line.charAt(i) == ' ')) {
            i++;
        }
        return i;
    }

    public static String removeOptionalComment(String line) {
        return line.startsWith(COMMENT) ? removeLeadingWhitespaces(line.substring(COMMENT.length())) : line;
    }

    public static String removeLeadingWhitespaces(String line) {
        int i = 0;
        while ((i < line.length()) && (line.charAt(i) == ' ')) {
            i++;
        }
        return line.substring(i);
    }

    public static String remainderOfCommand(String line, int indent, String command) {
        for (int i = 0; i < indent; i++) {
            if (line.charAt(i) != ' ') {
                return null;
            }
        }
        String res = removeOptionalComment(line.substring(indent));
        if (res.startsWith(command)) {
            return res.substring(command.length());
        }
        return null;
    }

    public static boolean startsWith(String line, String start) {
        return removeOptionalComment(removeLeadingWhitespaces(line)).startsWith(start);
    }

    public static String createIndent(int indent) {
        return " ".repeat(Math.max(0, indent));
    }

    public static List<LineInFile> readLines(Path path) {
        try {
            return addPath(Files.readAllLines(path), path);
        } catch (IOException e) {
            throw new RuntimeException("Problem reading file "+path, e);
        }
    }

    /**
     * Create a list using insertedLines prefixed with indentation,
     * then append lines starting from #1 (discard lines,get(0))
     */
    public static List<LineInFile> combine(List<LineInFile> lines, List<LineInFile> insertedLines, int indent) {
        return combine(lines, insertedLines, createIndent(indent));
    }

    public static List<LineInFile> combine(List<LineInFile> lines, List<LineInFile> insertedLines, String prefix) {
        List<LineInFile> res = new ArrayList<>();
        insertedLines.forEach(l -> res.add(l.changeString(prefix + l.line)));
        res.addAll(lines);
        return res;
    }

    public static String removeIndent(String line, final int indent) {
        for (int i = 0; i < indent; i++) {
            if ((i >= line.length()) || (line.charAt(i) != ' ')) {
                throw new RuntimeException("Indentation of line in macro body inconsistent");
            }
        }
        return line.substring(indent);
    }

    public static Path resolvePath(Path linePath, Path includeRoot, String includedString) {
        return includedString.startsWith("/")
                ? includeRoot.resolve(includedString.substring(1))
                : linePath.resolveSibling(includedString);
    }

    public static List<LineInFile> addPath(List<String> lines, Path path) {
        List<LineInFile> result = new ArrayList<>();
        int i = 0;
        for (String line :  lines) {
            result.add(new LineInFile(line, path, ++i));
        }
        return result;
    }

    public static List<LineInFile> copyPathAndNumber(List<String> lines, LineInFile origin) {
        return lines.stream().map(origin::changeString).collect(Collectors.toList());
    }
}
