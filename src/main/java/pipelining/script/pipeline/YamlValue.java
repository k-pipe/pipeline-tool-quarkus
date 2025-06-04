package pipelining.script.pipeline;

import java.util.LinkedList;
import java.util.List;

public class YamlValue implements YamlElement {
    private final String value;
    private final boolean quoted;

    public YamlValue(String value, boolean quoted) {
        this.value = value;
        this.quoted = quoted;
    }

    @Override
    public List<String> getLines() {
        List<String> res = new LinkedList<>();
        res.add(quoted ? "\""+value+"\"" : value);
        return res;
    }
}
