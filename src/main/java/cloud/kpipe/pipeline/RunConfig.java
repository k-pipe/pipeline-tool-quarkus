package cloud.kpipe.pipeline;

import com.kneissler.script.pipeline.YamlMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class RunConfig {

    private final String name;
    private Map<String, String> map = new LinkedHashMap<>();

    public RunConfig(String name) {
        this.name = name;
    }

    public void addToYaml(YamlMap parameters) {
        map.forEach((k,v) -> {
            parameters.add(k, v);
        });
    }

    public void put(String key, String value) {
        map.put(key, value);
    }
}
