package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class KtbTopic {
    String name;
    Map<String, String> config;

    @JsonCreator
    public KtbTopic(
            @JsonProperty("name") String name,
            @JsonProperty("config") Map<String, String> config) {
        this.name = name;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "KtbTopic{" +
                "name='" + name + '\'' +
                ", config=" + config +
                '}';
    }
}
