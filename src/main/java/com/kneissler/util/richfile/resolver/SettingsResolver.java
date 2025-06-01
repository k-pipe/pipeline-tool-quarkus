package com.kneissler.util.richfile.resolver;

import com.kneissler.util.richfile.Constants;
import com.kneissler.util.richfile.LineInFile;
import com.kneissler.util.richfile.ResolveUtil;
import com.kneissler.util.richfile.Resolver;

import java.nio.file.Path;
import java.util.List;

public class SettingsResolver implements Resolver {

    private final Path includeRoot;
    private final List<Path> usedFiles;

    public SettingsResolver(Path includeRoot, List<Path> usedFiles) {
        this.includeRoot = includeRoot;
        this.usedFiles = usedFiles;
    }

    @Override
    public boolean canResolve(String line) {
        return ResolveUtil.startsWith(line, Constants.SETTINGS);
    }

    @Override
    public List<LineInFile> resolve(LineInFile firstLine, List<LineInFile> remainingLines) {
        int indent = ResolveUtil.determineIndent(firstLine.line);
        String included = ResolveUtil.remainderOfCommand(firstLine.line, indent, Constants.SETTINGS);
        if (included == null) {
            throw new RuntimeException("Illegal settings command");
        }
        Path includedPath = ResolveUtil.resolvePath(firstLine.path, includeRoot, included).normalize();
        usedFiles.add(includedPath);
        String prefix = ResolveUtil.createIndent(indent)+ Constants.SET+" ";
        return ResolveUtil.combine(remainingLines, ResolveUtil.readLines(includedPath), prefix);
    }

}
