package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Model class for a topic in a Kafka cluster.
 */
public class TopicDescription {
    private final String name;
    private final Map<String, String> configs;
    private final TopicDataModel dataModel;

    @JsonGetter
    public String name() {
        return name;
    }
    @JsonGetter
    public Map<String, String> configs() {
        return configs;
    }

    @JsonGetter("dataModel")
    public TopicDataModel dataModel() { return dataModel; }

    @JsonCreator
    public TopicDescription(
            @JsonProperty("name") String name,
            @JsonProperty("configs") Map<String, String> configs,
            @JsonProperty("dataModel") TopicDataModel dataModel
    ) {
        this.name = name;
        this.configs = configs;
        this.dataModel = dataModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicDescription)) return false;
        TopicDescription that = (TopicDescription) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(configs, that.configs) &&
                Objects.equals(dataModel, that.dataModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, configs, dataModel);
    }

    @Override
    public String toString() {
        return "TopicDescription{" +
                "name='" + name + '\'' +
                ", configs=" + configs +
                ", dataModel=" + dataModel +
                '}';
    }
}
