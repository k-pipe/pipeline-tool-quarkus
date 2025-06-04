package pipelining.clparser;

import pipelining.logging.Log;
import pipelining.util.Expect;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static pipelining.application.Application.fail;

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
        for (String arg : args) {
            if (!arg.trim().startsWith("#")) {
                Command command = getPossibleCommand(arg);
                if (command != null) {
                    Command newCommand;
                    newCommand = command.clone();
                    commonOptions.forEach(o -> {
                        if (newCommand.optionAdded(o)) commonOptionUsed.add(o);
                    });
                    actualCommands.add(newCommand);
                    lastCommand = newCommand;
                } else if (lastCommand == null) {
                    commonOptions.add(arg);
                } else {
                    Expect.isTrue(lastCommand.optionAdded(arg)).elseFail("Unexpected option key: " + arg);
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
        for (String o : commonOptions) {
            Expect.isTrue(commonOptionUsed.contains(o)).elseFail("Option not used by any command: "+o);
        }
    }

    private Command getPossibleCommand(String arg) {
        return possibleCommands.stream().filter(c -> c.matches(arg)).findFirst().orElse(null);
    }

    public void showUsage() {
        Log.log("Syntax: pipeline-tool [option]* [command [option]*]*");
        Log.log("Possible commands:");
        for (Command c : possibleCommands) {
            c.logUsage();
        }
    }

    public List<Command> getCommands() {
        return actualCommands;
    }
}
