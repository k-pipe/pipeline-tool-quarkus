package com.kneissler.script.pipeline;

import java.util.LinkedList;
import java.util.List;

public class YamlList implements YamlElement {
    private final List<YamlElement> list = new LinkedList<>();

    public <Y extends YamlElement> YamlList add(Y item) {
        list.add(item);
        return this;
    }

    @Override
    public List<String> getLines() {
        List<String> res = new LinkedList<>();
        for (YamlElement e : list) {
            boolean first = true;
            for (String line : e.getLines()) {
                res.add((first ? Yaml.LIST_ITEM : Yaml.INDENT)+line);
                first = false;
            }
        }
        return res;
    }
}
