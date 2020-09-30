package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KtbProject {
    List<KtbTopic> topics;
    String name;
    List<KtbConsumer> consumers;
    List<KtbConnector> connectors;
    List<KtbProducer> producers;
    List<KtbStreamsApp> streams;

    @JsonCreator
    public KtbProject(
            @JsonProperty("name") String name,
            @JsonProperty("topics") List<KtbTopic> topics,
            @JsonProperty("consumers") List<KtbConsumer> consumers,
            @JsonProperty("connectors") List<KtbConnector> connectors,
            @JsonProperty("producers") List<KtbProducer> producers,
            @JsonProperty("streams") List<KtbStreamsApp> streams
    ) {
        this.name = name;
        this.topics = topics;
        // Jackson passes null reference when field not found
        this.consumers = consumers != null ? consumers : Collections.EMPTY_LIST;
        this.connectors = connectors != null ? connectors : Collections.EMPTY_LIST;
        this.producers = producers != null ? producers : Collections.EMPTY_LIST;
        this.streams = streams != null ? streams : Collections.EMPTY_LIST;
    }


    public List<KtbTopic> getTopics() {
        return topics;
    }

    public String getName() {
        return name;
    }



    public List<KtbConsumer> getConsumers() {
        return consumers;
    }

    public List<KtbConnector> getConnectors() {
        return connectors;
    }

    public List<KtbProducer> getProducers() {
        return producers;
    }

    public List<KtbStreamsApp> getStreams() {
        return streams;
    }

    @Override
    public String toString() {
        return "KtbProject{" +
                "topics=" + topics +
                ", name='" + name + '\'' +
                ", consumers=" + consumers +
                ", connectors=" + connectors +
                ", producers=" + producers +
                ", streams=" + streams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KtbProject)) return false;
        KtbProject that = (KtbProject) o;
        return Objects.equals(topics, that.topics) &&
                Objects.equals(name, that.name) &&
                Objects.equals(consumers, that.consumers) &&
                Objects.equals(connectors, that.connectors) &&
                Objects.equals(producers, that.producers) &&
                Objects.equals(streams, that.streams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topics, name, consumers, connectors, producers, streams);
    }

    /**
     * Implements the builder pattern.
     */

    public static Builder builder(String projectName) {
        return new Builder(projectName);
    }

    public static class Builder {
        List<KtbTopic> topics = Collections.EMPTY_LIST;
        String name;
        List<KtbConsumer> consumers = Collections.EMPTY_LIST;
        List<KtbConnector> connectors = Collections.EMPTY_LIST;
        List<KtbProducer> producers = Collections.EMPTY_LIST;
        List<KtbStreamsApp> streamsApps = Collections.EMPTY_LIST;

        private Builder(String name) {
            this.name = name;
        }

        public Builder withTopics(List<KtbTopic> topics) {
            this.topics = topics;
            return this;
        }

        public Builder withConsumers(List<KtbConsumer> consumers) {
            this.consumers = consumers;
            return this;
        }

        public Builder withConsumer(KtbConsumer consumer) {
            this.consumers = Collections.singletonList(consumer);
            return this;
        }

        public Builder withConnectors(List<KtbConnector> connectors) {
            this.connectors = connectors;
            return this;
        }

        public Builder withProducers(List<KtbProducer> producers) {
            this.producers = producers;
            return this;
        }

        public Builder withStreams(List<KtbStreamsApp> streams) {
            this.streamsApps = streams;
            return this;
        }

        public KtbProject build() {
            return new KtbProject(name, topics, consumers, connectors, producers, streamsApps);
        }
    }
}
