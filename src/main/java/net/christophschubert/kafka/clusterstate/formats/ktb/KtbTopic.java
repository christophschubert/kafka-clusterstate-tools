package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class KtbTopic {
    String name;
    Map<String, String> config;

    @JsonCreator
    public KtbTopic(
            @JsonProperty("name") String name,
            @JsonProperty("config") Map<String, String> config) {
        this.name = name;
        this.config = config != null ? config : Collections.EMPTY_MAP;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KtbTopic)) return false;
        KtbTopic ktbTopic = (KtbTopic) o;
        return Objects.equals(name, ktbTopic.name) &&
                Objects.equals(config, ktbTopic.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, config);
    }
}
