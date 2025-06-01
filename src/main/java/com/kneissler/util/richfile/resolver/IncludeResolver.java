package com.kneissler.util.richfile.resolver;

import com.kneissler.util.richfile.Constants;
import com.kneissler.util.richfile.LineInFile;
import com.kneissler.util.richfile.ResolveUtil;
import com.kneissler.util.richfile.Resolver;

import java.nio.file.Path;
import java.util.List;

public class IncludeResolver implements Resolver {

    private final Path includeRoot;
    private final List<Path> usedFiles;

    public IncludeResolver(Path includeRoot, List<Path> usedFiles) {
        this.includeRoot = includeRoot;
        this.usedFiles = usedFiles;
    }

    @Override
    public boolean canResolve(String line) {
        return ResolveUtil.startsWith(line, Constants.INCLUDE);
    }

    @Override
    public List<LineInFile> resolve(LineInFile firstLine, List<LineInFile> remainingLines) {
        int indent = ResolveUtil.determineIndent(firstLine.line);
        String included = ResolveUtil.remainderOfCommand(firstLine.line, indent, Constants.INCLUDE);
        if (included == null) {
            throw new RuntimeException("illegal include command");
        }
        Path includedPath = ResolveUtil.resolvePath(firstLine.path, includeRoot, included).normalize();
        usedFiles.add(includedPath);
        return ResolveUtil.combine(remainingLines, ResolveUtil.readLines(includedPath), indent);
    }

}
