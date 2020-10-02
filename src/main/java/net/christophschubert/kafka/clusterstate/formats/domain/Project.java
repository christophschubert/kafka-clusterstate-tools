package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class Project {

    public Domain parent;

    @JsonProperty("name")
    public final String name;

    @JsonProperty("topics")
    public final Set<Topic> topics;

    @JsonProperty("consumers")
    public final Set<Consumer> consumers;

    @JsonProperty("producers")
    public final Set<Producer> producers;

    @JsonProperty("streamsApps")
    public final Set<StreamsApp> streamsApps;

    void updateChildren() {
        setThisAsParent(topics);
        setThisAsParent(consumers);
        setThisAsParent(producers);
        setThisAsParent(streamsApps);
    }

    private void setThisAsParent(Collection<? extends ProjectSubResource> subResources) {
        subResources.forEach(c -> c.parent = this);
    }

    @JsonCreator
    public Project(
            @JsonProperty("name") String name,
            @JsonProperty("topics") Set<Topic> topics,
            @JsonProperty("consumers") Set<Consumer> consumers,
            @JsonProperty("producers") Set<Producer> producers,
            @JsonProperty("streamsApps") Set<StreamsApp> streamsApps
    ) {
        this.name = name;
        this.topics = Helpers.emptyForNull(topics);
        this.consumers = Helpers.emptyForNull(consumers);
        this.producers = Helpers.emptyForNull(producers);
        this.streamsApps = Helpers.emptyForNull(streamsApps);

        updateChildren();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return
                Objects.equals(name, project.name) &&
                Objects.equals(topics, project.topics) &&
                Objects.equals(consumers, project.consumers) &&
                Objects.equals(producers, project.producers) &&
                Objects.equals(streamsApps, project.streamsApps);
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, topics, consumers, producers, streamsApps);
    }

    @Override
    public String toString() {
        return "Project{" +
                "parent=" + parent.name +
                ", name='" + name + '\'' +
                ", topics=" + topics +
                ", consumers=" + consumers +
                ", producers=" + producers +
                ", streamsApps=" + streamsApps +
                '}';
    }
}
