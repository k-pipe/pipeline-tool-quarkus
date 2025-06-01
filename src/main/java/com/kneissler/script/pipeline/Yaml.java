package com.kneissler.script.pipeline;

import java.util.List;

public class Yaml extends YamlMap {

    public static final String INDENT = "  ";
    public static final String LIST_ITEM = "- ";

    public static YamlMap map() {
        return new YamlMap();
    }
    public static YamlList list() {
        return new YamlList();
    }

    public static YamlValue value(String value) {
        return Yaml.value(value, false);
    }

    public static YamlValue value(String value, boolean quoted) {
        return new YamlValue(value, quoted);
    }

    public static YamlValue string(String value) {
        return value(value, true);
    }

    public static YamlSubYaml subYaml(List<String> lines) {
        return new YamlSubYaml(lines);
    }

}
