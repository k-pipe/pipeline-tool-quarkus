package pipelining.util.richfile.resolver;

import pipelining.util.richfile.Constants;
import pipelining.util.richfile.LineInFile;
import pipelining.util.richfile.ResolveUtil;
import pipelining.util.richfile.Resolver;

import java.util.ArrayList;
import java.util.List;

public class IfResolver implements Resolver {

    private static final String NO = "no";
    private static final String FALSE = "false";
    private static final String ZERO = "0";

    public enum IfStage {
        THEN, ELSE, AFTER
    }

    @Override
    public boolean canResolve(String line) {
        return ResolveUtil.startsWith(line, Constants.IF);
    }

    @Override
    public List<LineInFile> resolve(LineInFile firstLine, List<LineInFile> remainingLines) {
        List<LineInFile> res = new ArrayList<>();
        int indent = ResolveUtil.determineIndent(firstLine.line);
        String conditionString = ResolveUtil.remainderOfCommand(firstLine.line, indent, Constants.IF);
        if (conditionString == null) {
            throw new RuntimeException("illegal if command");
        }
        boolean conditionSatisfied = evaluateCondition(conditionString);
        IfStage stage = IfStage.THEN;
        for (LineInFile line : remainingLines) {
            switch (stage) {
                case THEN:
                    if (ResolveUtil.remainderOfCommand(line.line, indent, Constants.ELSE) != null) {
                        stage = IfStage.ELSE;
                    } else if (ResolveUtil.remainderOfCommand(line.line, indent, Constants.END) != null) {
                        stage = IfStage.AFTER;
                    } else if (conditionSatisfied) {
                        res.add(line);
                    }
                    break;
                case ELSE:
                    if (ResolveUtil.remainderOfCommand(line.line, indent, Constants.END) != null) {
                        stage = IfStage.AFTER;
                    } else if (!conditionSatisfied) {
                        res.add(line);
                    }
                    break;
                case AFTER:
                    res.add(line);
                    break;
            }
        }
        return res;
    }

    private boolean evaluateCondition(final String condition) {
        String[] split = condition.split("=");
        boolean result = false;
        if (split.length == 1) {
            result = !split[0].isBlank() && !split[0].equalsIgnoreCase(NO) && !split[0].equalsIgnoreCase(FALSE)
                     && !split[0].equals(ZERO);
            System.out.println("Condition exist "+split[0]+" evaluates to "+result);
        }
        if (split.length == 2) {
            result = split[0].trim().equals(split[1].trim());
            System.out.println(" Condition "+split[0]+" equals "+split[1]+" evaluates to "+result);
        }
        return result;
    }

}
