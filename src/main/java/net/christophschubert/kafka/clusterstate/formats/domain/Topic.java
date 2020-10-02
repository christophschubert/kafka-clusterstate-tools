package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Topic extends ProjectSubResource {


    @JsonProperty("name")
    public final String name;
    @JsonProperty("configs")
    public final Map<String, String> configs;


    @JsonCreator
    public Topic(
            @JsonProperty("name") String name,
            @JsonProperty("configs") Map<String, String> configs
    ) {
        this.name = name;
        this.configs = configs;
    }



    // for quick test case creation
    Topic(String name) {
        this.name = name;
        this.configs = Collections.emptyMap();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        Topic topic = (Topic) o;
        return Objects.equals(configs, topic.configs) &&
                Objects.equals(name, topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, configs);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                ", parent='" + parent.name + '\'' +
                ", configs=" + configs +
                '}';
    }

    @Override
    public String id() {
        return name;
    }
}
