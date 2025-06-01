package com.kneissler.script.pipeline;

import java.util.List;

public class YamlSubYaml implements YamlElement {
    private final List<String> lines;

    public YamlSubYaml(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public List<String> getLines() {
        return lines;
    }
}
