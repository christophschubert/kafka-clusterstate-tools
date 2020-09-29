package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.List;

public class KtbProject {
    List<KtbTopic> topics;
    String name;

    @JsonCreator
    public KtbProject(
            @JsonProperty("name") String name,
            @JsonProperty("topics") List<KtbTopic> topics) {
        this.name = name;
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "KtbProject{" +
                "topics=" + topics +
                ", name='" + name + '\'' +
                '}';
    }

    public List<KtbTopic> getTopics() {
        return topics;
    }

    public String getName() {
        return name;
    }
}
