package pipelining.clparser;

import pipelining.util.Expect;

import java.util.*;
import java.util.stream.Collectors;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.log;

public class CommandArgsParser {
    private final List<Command> possibleCommands = new LinkedList<>();
    private final List<Command> actualCommands = new LinkedList<>();

    public static CommandArgsParser create() {
        return new CommandArgsParser();
    }

    public CommandArgsParser command(String key, String description, Option... options) {
        possibleCommands.forEach(c -> {
            if (c.matches(key)) {
                fail("duplicate command definition: "+key);
            }
        });
        possibleCommands.add(new Command(key, description, options));
        return this;
    }

    public void parse(String[] args) {
        Command lastCommand = null;
        List<String> commonOptions = new LinkedList<>();
        Set<String> commonOptionUsed = new HashSet<>();
        LinkedList<String> argsqueue = new LinkedList<>();
        for (String arg : args) {
            argsqueue.add(arg);
        }
        while (!argsqueue.isEmpty()) {
            String arg = argsqueue.pop();
            if (!arg.trim().startsWith("#")) {
                Command command = getPossibleCommand(arg);
                if (command != null) {
                    Command newCommand;
                    newCommand = command.clone();
                    LinkedList<String> commonOptionsQueue = new LinkedList<>(commonOptions);
                    while (!commonOptionsQueue.isEmpty()) {
                        String o = commonOptionsQueue.pop();
                        if (newCommand.optionAdded(o, commonOptionsQueue, commonOptionUsed)) {
                            commonOptionUsed.add(o);
                        }
                    }
                    actualCommands.add(newCommand);
                    lastCommand = newCommand;
                } else if (lastCommand == null) {
                    commonOptions.add(arg);
                } else {
                    Expect.isTrue(lastCommand.optionAdded(arg, argsqueue, null)).elseFail("Unexpected option: " + arg);
                }
            }
        }
        setDefaultValues();
        checkAllMandatorySet();
        checkAllCommonOptionsUsed(commonOptions, commonOptionUsed);
    }

    private void setDefaultValues() {
        for (Command c : actualCommands) {
            for (Option o : c.getPossibleOptions()) {
                if (o.hasDefault() && !c.isSet(o)) {
                    c.setDefaultValue(o);
                }
            }
        }
    }

    private void checkAllMandatorySet() {
        for (Command c : actualCommands) {
            for (Option o : c.getPossibleOptions()) {
                Expect.isTrue(o.usedOrOptional()).elseFail("mandatory option was not set: "+o.getKey()+" in command "+c.getKey());
            }
        }
    }

    private void checkAllCommonOptionsUsed(List<String> commonOptions, Set<String> commonOptionUsed) {
        Set<String> unused = new LinkedHashSet<>(commonOptions);
        unused.removeAll(commonOptionUsed);
        if (!unused.isEmpty()) {
            log("Some option were not used by any command: "+unused.stream().collect(Collectors.joining(", ")));
        }
    }

    private Command getPossibleCommand(String arg) {
        return possibleCommands.stream().filter(c -> c.matches(arg)).findFirst().orElse(null);
    }

    public void showUsage() {
        log("Syntax: pipeline-tool [option]* [command [option]*]*");
        log("Possible commands:");
        for (Command c : possibleCommands) {
            c.logUsage();
        }
    }

    public List<Command> getCommands() {
        return actualCommands;
    }
}
