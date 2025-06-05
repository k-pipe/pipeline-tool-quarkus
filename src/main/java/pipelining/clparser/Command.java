package pipelining.clparser;

import pipelining.logging.Log;
import pipelining.util.Expect;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pipelining.clparser.Option.OPTION_SEPARATOR;

public class Command {
    private final String key;
    private final String description;
    private List<Option> possibleOptions;
    private final Map<Option, List<String>> actualOptions = new LinkedHashMap<>();

    public Command(String key, String description, List<Option> options) {
        this.key = key;
        this.description = description;
        this.possibleOptions = options;
    }

    public Command(String key, String description, Option... options) {
        this(key, description, List.of(options));
    }

    @Override
    public Command clone() {
        Command cloned = new Command(key, description, possibleOptions);
        cloned.possibleOptions = this.possibleOptions;
        return cloned;
    }

    public boolean matches(String key) {
        return this.key.equals(key);
    }

    public boolean optionAdded(String optionString, LinkedList<String> argsqueue, Set<String> argumentsUsed) {
        String[] split = optionString.split(OPTION_SEPARATOR, 2);
        String keyWithPrefix = split[0].trim();
        String value = split.length == 2 ? split[1].trim() : null;
        Option option = findMatchingOption(keyWithPrefix);
        if (option == null) {
            return false;
        }
        if (option.hasValue()) {
            if (value == null) {
                value = argsqueue.pop();
                if (argumentsUsed != null) {
                    argumentsUsed.add(value);
                }
            }
        } else {
            Expect.isNull(value).elseFail("option of command "+key+" is not supposed to have a value: "+keyWithPrefix);
        }
        addValue(option, value);
        return true;
    }

    public void addValue(Option option, String value) {
        option.setWasUsed();
        List<String> values = actualOptions.get(option);
        if (values == null) {
            values = new LinkedList<>();
            actualOptions.put(option, values);
        } else {
            Expect.isTrue(option.isCanBeRepeated()).elseFail("multiple option for non repeatable option: command="+key+", option="+ option.getKey());
        }
        if (value != null) {
            values.add(value);
        }
    }

    private Option findMatchingOption(String keyWithPrefix) {
        for (Option po : possibleOptions) {
            Option mo = po.getOrCreateMatchingOption(keyWithPrefix);
            if (mo != null) {
                return mo;
            }
        }
        return null;
    }

    private Option getOptionByLongKey(String longKey) {
        return possibleOptions.stream().filter(o -> o.getKey().equals(longKey)).findFirst().orElse(null);
    }

    public List<Option> getPossibleOptions() {
        return possibleOptions;
    }

    public String getKey() {
        return key;
    }

    public void logUsage() {
        Log.log("   "+key+": "+description);
        for (Option o : possibleOptions) {
            o.logUsage();
        }
    }

    public Map<Option, List<String>> getOptions() {
        return actualOptions;
    }

    public boolean isSet(Option o) {
        return actualOptions.containsKey(o);
    }

    public void setDefaultValue(Option o) {
        Log.log("Using default value for option "+o.getKey()+" in command "+key+": "+o.getDefault());
        addValue(o, o.getDefault());
    }

    public String logString() {
        return key+": "+getOptions().entrySet()
            .stream()
            .map(e -> e.getKey().getKey()+"="+e.getValue().stream().collect(Collectors.joining(",")))
            .collect(Collectors.joining(" "));
    }

    public List<String> getOptionValues(String optionKey) {
        Option option = getOptionByLongKey(optionKey);
        Expect.notNull(option).elseFail("No such option in command "+key+": "+optionKey);
        List<String> res = actualOptions.get(option);
        Expect.notNull(res).elseFail("Option expected in command "+key+" but was not set: "+optionKey);
        return res;
    }

    public String getOptionValue(String optionKey) {
        List<String> values = getOptionValues(optionKey);
        Expect.equal(values.size(), 1).elseFail("Expected exactly one value in command "+key+" for option: "+optionKey);
        return values.get(0);
    }

    public Optional<String> getOptionalOptionValue(String optionKey) {
        Option option = getOptionByLongKey(optionKey);
        Expect.notNull(option).elseFail("No such option in command "+key+": "+optionKey);
        Expect.isTrue(option.isOptional()).elseFail("The option is not optional, but used as if it was: "+optionKey);
        List<String> values = actualOptions.get(option);
        if ((values == null) || values.isEmpty())
            return Optional.empty();
        Expect.equal(values.size(), 1).elseFail("Expected at most one value in command "+key+" for option: "+optionKey);
        return Optional.of(values.get(0));
    }

    public boolean isFlagSet(String optionKey) {
        Option option = getOptionByLongKey(optionKey);
        Expect.notNull(option).elseFail("No such option in command " + key + ": " + optionKey);
        Expect.isTrue(option.isOptional()).elseFail("The option is not optional, but used as if it was: " + optionKey);
        return isSet(option);
    }

    public Stream<Map.Entry<Option, List<String>>> getWildcardOptions() {
        return actualOptions.entrySet().stream().filter(e -> e.getKey().isWildcard());
    }
}
