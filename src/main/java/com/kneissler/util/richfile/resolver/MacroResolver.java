package com.kneissler.util.richfile.resolver;

import com.kneissler.util.richfile.*;

import java.util.ArrayList;
import java.util.List;

public class MacroResolver implements Resolver {

    List<Macro> macros = new ArrayList<>();

    @Override
    public boolean canResolve(String line) {
        if (ResolveUtil.startsWith(line, Constants.MACRO)) {
            return true;
        }
        for (Macro m : macros) {
            if (ResolveUtil.startsWith(line, Constants.MARKER+m.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<LineInFile> resolve(LineInFile firstLine, List<LineInFile> remainingLines) {
        if (ResolveUtil.startsWith(firstLine.line, Constants.MACRO)) {
            return extractMacroDefinition(firstLine, remainingLines);
        } else {
            int indent = ResolveUtil.determineIndent(firstLine.line);
            for (Macro m : macros) {
                String remain = ResolveUtil.remainderOfCommand(firstLine.line, indent, Constants.MARKER+m.getName());
                if (remain != null) {
                    return ResolveUtil.combine(remainingLines, ResolveUtil.copyPathAndNumber(m.resolve(remain), firstLine), indent);
                }
            }
        }
        return null;
    }

    private List<LineInFile> extractMacroDefinition(LineInFile firstLine, List<LineInFile> remainingLines) {
        List<LineInFile> result = new ArrayList<>();
        int indent = ResolveUtil.determineIndent(firstLine.line);
        String macroString = ResolveUtil.remainderOfCommand(firstLine.line, indent, Constants.MACRO);
        if (macroString == null) {
            throw new RuntimeException("illegal macro definition");
        }
        Macro macro = parseMacroHeader(macroString);
        boolean done = false;
        for (LineInFile line : remainingLines) {
            if (done) {
                result.add(line);
            } else if (ResolveUtil.remainderOfCommand(line.line, indent, Constants.END) != null) {
                done = true;
            } else {
                macro.addLine(ResolveUtil.removeIndent(line.line, indent));
            }
        }
        macros.add(macro);
        return result;
    }

    private Macro parseMacroHeader(final String nameAndArgNum) {
        String[] split = nameAndArgNum.split(" ");
        if (split.length != 2) {
            throw new RuntimeException("Expected format #MACRO name numArgs");
        }
        return new Macro(split[0], Integer.parseInt(split[1]));
    }

}
