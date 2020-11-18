package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Topic extends ProjectSubResource {

    @JsonProperty("name")
    public final String name;
    @JsonProperty("configs")
    public final Map<String, String> configs;

    @JsonProperty("dataModel")
    public final DataModel dataModel;

    // additional principals (not defined in this project) which have read access to the topic
    @JsonProperty("consumerPrincipals")
    public final Set<String> consumerPrincipals;

    // additional principals (not defined in this project) which have write access to the topic
    @JsonProperty("producerPrincipals")
    public final Set<String> producerPrincipals;

    @JsonCreator
    public Topic(
            @JsonProperty("name") String name,
            @JsonProperty("configs") Map<String, String> configs,
            @JsonProperty("dataModel") DataModel dataModel,
            @JsonProperty("consumerPrincipals") Set<String> consumerPrincipals,
            @JsonProperty("producerPrincipals") Set<String> producerPrincipals
    ) {
        Objects.requireNonNull(name, "Topic name cannot be null");
        this.name = name;
        this.configs = configs == null ? Collections.emptyMap() : configs;
        this.dataModel = dataModel;
        this.consumerPrincipals = Helpers.emptyForNull(consumerPrincipals);
        this.producerPrincipals = Helpers.emptyForNull(producerPrincipals);
    }


    // for quick test case creation
    Topic(String name) {
        this.name = name;
        this.configs = Collections.emptyMap();
        this.dataModel = null;
        this.consumerPrincipals = Collections.emptySet();
        this.producerPrincipals = Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        Topic topic = (Topic) o;
        return Objects.equals(name, topic.name) &&
                Objects.equals(configs, topic.configs) &&
                Objects.equals(dataModel, topic.dataModel) &&
                Objects.equals(consumerPrincipals, topic.consumerPrincipals) &&
                Objects.equals(producerPrincipals, topic.producerPrincipals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, configs, dataModel, consumerPrincipals, producerPrincipals);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                ", configs=" + configs +
                ", dataModel=" + dataModel +
                ", consumerPrincipals=" + consumerPrincipals +
                ", producerPrincipals=" + producerPrincipals +
                '}';
    }

    @Override
    public String id() {
        return name;
    }
}
