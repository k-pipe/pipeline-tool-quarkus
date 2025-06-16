package pipelining.util.richfile.resolver;

import pipelining.util.Expect;
import pipelining.util.richfile.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class VariableResolver implements Resolver {

    private final Map<String, String> variables;

    public VariableResolver(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public boolean canResolve(String line) {
        return ResolveUtil.startsWith(line, Constants.SET);
    }

    @Override
    public List<LineInFile> resolve(LineInFile firstLine, List<LineInFile> remainingLines) {
        int indent = ResolveUtil.determineIndent(firstLine.line);
        setVariable(ResolveUtil.remainderOfCommand(firstLine.line, indent, Constants.SET));
        return remainingLines;
    }

    private void setVariable(final String setString) {
        if (setString == null) {
            throw new RuntimeException("Invalid emtpy set string");
        }
        String[] split = setString.trim().split(" ",2);
        if (split.length != 2) {
            throw new RuntimeException("Illegal syntax of SET: "+setString);
        }
        variables.put(split[0], split[1]);
    }

    public String substituteVariables(final String line) {
        StringBuilder result = new StringBuilder();
        String remain = line;
        Matcher m;
        while ((m = Constants.PROPERTY_REGEX.matcher(remain)).matches()) {
            String var = m.group(1);
            int start = m.start(1)-2;
            int end = m.end(1)+1;
            if ((start > 0) && (remain.charAt(start-1) == '$')) {
                result.append(substituteVariables(remain.substring(0, start-1)));
                result.append(remain, start, end);
                remain = remain.substring(end);
            } else {
                String value = variables.get(var);
                Expect.notNull(value).elseFail("The variable is not defined: "+var);
                String oldremain = remain;
                remain = Macro.replace(remain, start, end, value);
                if (remain.equals(oldremain)) {
                    break;
                }
            }
        }
        result.append(remain);
        return result.toString();
    }

}
