package com.kneissler.util.richfile;

import com.kneissler.util.richfile.resolver.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Text Files that support the following macro extensions (given a key-value map as input):
 *  - include other files: #INCLUDE file
 *  - nested options:
 *      #IF ${key}[=value]
 *         ...
 *      #ELSE
 *         ...
 *      #END
 *    or
 *      #IF ${key}[=value]
 *         ...
 *      #END
 *  - set entries in key value map:  #SET key=value
 *  - set multiple key/values from a file: #SETTINGS file
 *  - resolve properties: ${key}
 *  - define macro:
 *     #MACRO name #
 *      ... (use args like this: [0] [1] etx,)
 *     #END
 *   - use macro:
 *     $name(value1|value2|...)
 *
 * Note: resolution/include can be iterated (resolve properties in keys/values/filenames) and
 * include files from included files, use macros within macro definitions
 */
public class RichFile {

    private final Path includeRoot;
    private final Path mainFile;
    private final List<Path> usedFiles;
    private final List<Resolver> primaryResolvers;
    private final List<Resolver> secondaryResolvers;

    public static List<String> resolveVariables(final List<String> lines, final Map<String, String> variables) {
        return new RichFile().resolveLines(ResolveUtil.addPath(lines, Path.of("")), variables);
    }

    public RichFile(Path includeRoot, Path mainFile) {
        this.includeRoot = includeRoot;
        this.mainFile = mainFile;
        this.usedFiles = new ArrayList<>();
        this.primaryResolvers = createPrimaryResolvers();
        this.secondaryResolvers = createSecondaryResolvers();
    }

    public RichFile(Path mainFile) {
        this(mainFile.getParent(), mainFile);
    }

    public RichFile() {
        this(null, null);
    }

    private List<Resolver> createPrimaryResolvers() {
        List<Resolver> res = new ArrayList<>();
        if (includeRoot != null) {
            res.add(new IncludeResolver(includeRoot, usedFiles));
            res.add(new SettingsResolver(includeRoot, usedFiles));
        }
        res.add(new MacroResolver());
        return res;
    }

    private List<Resolver> createSecondaryResolvers() {
        List<Resolver> res = new ArrayList<>();
        res.add(new IfResolver());
        return res;
    }

    public List<String> resolve() {
        return resolve(new LinkedHashMap<>());
    }

    public List<String> resolve(Map<String, String> variables) {
        usedFiles.add(mainFile);
        return resolveLines(ResolveUtil.readLines(mainFile), variables);
    }

    public List<Path> getUsedFiles() {
        return usedFiles;
    }

    private List<String> resolveLines(List<LineInFile> lines, Map<String, String> variables) {
        return resolveLinesWithPath(lines, variables).stream().map(l -> l.line).collect(Collectors.toList());
    }

    private List<LineInFile> resolveLinesWithPath(List<LineInFile> lines, Map<String, String> variables) {
        List<LineInFile> result = new ArrayList<>();
        List<LineInFile> remain = new ArrayList<>(lines);
        VariableResolver variableResolver = new VariableResolver(variables);
        while (!remain.isEmpty()) {
            remain = resolveFirst(remain, variableResolver, result);
        }
        return result;
    }

    private List<LineInFile> resolveFirst(List<LineInFile> lines, VariableResolver variableResolver, List<LineInFile> result) {
        LineInFile first = lines.get(0);
        List<LineInFile> remainingLines = lines.subList(1, lines.size());
        for (Resolver resolver : primaryResolvers) {
            if (resolver.canResolve(first.line)) {
                return resolver.resolve(first, remainingLines);
            }
        }
        if (variableResolver.canResolve(first.line)) {
            return variableResolver.resolve(first, remainingLines);
        }
        LineInFile substituted = first.changeString(variableResolver.substituteVariables(first.line));
        for (Resolver resolver : secondaryResolvers) {
            if (resolver.canResolve(substituted.line)) {
                return resolver.resolve(substituted, remainingLines);
            }
        }
        result.add(substituted);
        return remainingLines;
    }

}
