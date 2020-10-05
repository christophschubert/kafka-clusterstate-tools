package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TopicDataModel {
    @JsonProperty("key")
    public final SerializationInfo key;

    @JsonProperty("value")
    public final SerializationInfo value;

    @JsonCreator
    public TopicDataModel(
            @JsonProperty("key") SerializationInfo key,
            @JsonProperty("value") SerializationInfo value
    ) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicDataModel)) return false;
        TopicDataModel that = (TopicDataModel) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "TopicDataModel{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
