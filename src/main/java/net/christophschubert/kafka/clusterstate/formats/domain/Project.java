package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.*;

public class Project {
    @JsonIgnore
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

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }


    public static class Builder {
        Domain parent = null;
        Set<Topic> topics = new HashSet<>();
        String name = "defaultProject";
        Set<Consumer> consumers = new HashSet<>();
        Set<Producer> producers = new HashSet<>();
        Set<StreamsApp> streamsApps = new HashSet<>();

        private Builder() {}

        Builder(String name) {
            this.name = name;
        }

        public Builder addConsumer(Consumer c) {
            consumers.add(c);
            return this;
        }

        public Builder addConsumer(String principal, String groupId) {
            consumers.add(new Consumer(principal, groupId, false, null));
            return this;
        }

        public Builder addProducer(Producer p) {
            producers.add(p);
            return this;
        }

        public Builder addProducer(String principal) {
            producers.add(new Producer(principal, null, false, null));
            return this;
        }

        public Builder addTopic(Topic topic) {
            topics.add(topic);
            return this;
        }

        public Builder addStreamsApp(StreamsApp app) {
            streamsApps.add(app);
            return this;
        }

        public Builder addTopic(String topicName) {
            topics.add(new Topic(topicName));
            return this;
        }

        public Project build() {
            final Project p = new Project(
                    name,
                    topics,
                    consumers,
                    producers,
                    streamsApps
            );
            //make sure that p has a proper parent:
            if (parent == null)
                new Domain("defaultDomain", Set.of(p));
            return p;
        }
    }
}
