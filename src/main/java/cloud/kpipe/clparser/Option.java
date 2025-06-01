package cloud.kpipe.clparser;

import org.jkube.logging.Log;
import org.jkube.util.Expect;

public class Option {

    public static final String INDENT = "      ";
    static final String SHORT_OPTION_PREFIX = "-";
    static final String LONG_OPTION_PREFIX = "--";
    static final String OPTION_SEPARATOR = "=";
    public static final String OPTION_WILDCARD = "*"; // matches any option key

    private final String shortKey, longKey;
    private final String description;
    private final boolean hasValue;
    private boolean canBeRepeated;
    private boolean isOptional;
    private String defaultValue;
    private boolean wasUsed = false;

    public Option(String shortKey, String longKey, boolean canBeRepeated, boolean hasValue, boolean isOptional, String description) {
        this.shortKey = shortKey;
        this.longKey = longKey;
        this.canBeRepeated = canBeRepeated;
        this.hasValue = hasValue;
        this.isOptional = isOptional;
        this.description = description;
    }

    public static Option withValue(String shortKey, String longKey, String description) {
        return new Option(shortKey, longKey, false, true, false, description);
    }

    public static Option withoutValue(String shortKey, String longKey, String description) {
        return new Option(shortKey, longKey, false, false, true, description);
    }

    public static Option catchAll(String description) {
        return new Option(OPTION_WILDCARD, OPTION_WILDCARD, false, true, true, description);
    }


    public Option repeatable() {
        canBeRepeated = true;
        return this;
    }

    public Option optional() {
        isOptional = true;
        return this;
    }

    public Option withDefault(String defaultValue) {
        Expect.isTrue(hasValue).elseFail("Cannot set default for option without value: "+longKey);
        this.defaultValue = defaultValue;
        return this;
    }

    public Option getOrCreateMatchingOption(String key) {
        if (shortKey.equals(OPTION_WILDCARD)) {
            if (!key.startsWith(LONG_OPTION_PREFIX)) {
                // only long keys are allowed for wildcard options
                return null;
            }
            // wildcard option received, create a new actual option, cloned from this replacing long key
            return new Option(OPTION_WILDCARD, key.substring(LONG_OPTION_PREFIX.length()), canBeRepeated, hasValue, isOptional, description);
        }
        if (key.equals(SHORT_OPTION_PREFIX+shortKey) || key.equals(LONG_OPTION_PREFIX +longKey)) {
            return this;
        }
        return null;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public boolean isCanBeRepeated() {
        return canBeRepeated;
    }

    public void setWasUsed() {
        wasUsed = true;
    }

    public boolean usedOrOptional() {
        return isOptional || wasUsed;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public String getKey() {
        return longKey;
    }

    public boolean isWildcard() {
        return shortKey.equals(OPTION_WILDCARD);
    }

    public void logUsage() {
        if (isWildcard()) {
            Log.log(INDENT + LONG_OPTION_PREFIX + longKey + ": " + description);
        } else {
            Log.log(INDENT + SHORT_OPTION_PREFIX + shortKey + ", " + LONG_OPTION_PREFIX + longKey + ": " + description);
        }
    }

    public boolean hasDefault() {
        return defaultValue != null;
    }

    public String getDefault() {
        return defaultValue;
    }
}
