package pipelining.script.pipeline;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YamlMap implements YamlElement {
    private final Map<String, YamlElement> map = new LinkedHashMap<>();

    public <Y extends YamlElement> YamlMap add(String key, Y value) {
        map.put(key, value);
        return this;
    }

    public YamlMap add(String key, String value) {
        if (value != null) {
            map.put(key, Yaml.string(value));
        }
        return this;
    }

    public YamlMap add(String key, String... values) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String v: values) {
            sb.append(first ? "" : ",").append("\""+v+"\"");
            first = false;
        }
        sb.append("]");
        map.put(key, Yaml.value(sb.toString()));
        return this;
    }

    public YamlMap add(String key, int value) {
        map.put(key, Yaml.value(String.valueOf(value), false));
        return this;
    }

    public YamlMap add(String key, boolean value) {
        map.put(key, Yaml.value(String.valueOf(value), false));
        return this;
    }

    @Override
    public List<String> getLines() {
        List<String> res = new LinkedList<>();
        for (Map.Entry<String,YamlElement> e : map.entrySet()) {
            if (e.getValue() instanceof YamlValue) {
                res.add(e.getKey()+": "+e.getValue().getLines().get(0));
            } else {
                res.add(e.getKey() + ":");
                for (String line : e.getValue().getLines()) {
                    res.add(Yaml.INDENT+line);
                }
            }
        }
        return res;
    }

    public YamlMap addMap(String key) {
        YamlMap res = new YamlMap();
        add(key, res);
        return res;
    }

    public YamlList addList(String key) {
        YamlList res = new YamlList();
        add(key, res);
        return res;
    }

}
