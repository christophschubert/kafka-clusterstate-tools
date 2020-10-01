package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Project {

    Domain parent;

    @JsonProperty("name")
    final String name;

    @JsonProperty("topics")
    final Set<Topic> topics;

    void updateChildren() {
        topics.forEach(topic -> topic.parent = this);
    }

    @JsonCreator
    public Project(
            @JsonProperty("name") String name,
            @JsonProperty("topics") Set<Topic> topics
    ) {
        this.name = name;
        this.topics = topics == null ? Collections.EMPTY_SET : topics;
        updateChildren();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return Objects.equals(name, project.name) &&
                Objects.equals(topics, project.topics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, topics);
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", parent='" + parent.name + '\'' +
                ", topics=" + topics +
                '}';
    }
}
