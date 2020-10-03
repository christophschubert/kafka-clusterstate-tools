package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TopicSchemaData {
    @JsonProperty("keySchemaFile")
    public final String keySchemaFile;

    @JsonProperty("valueSchemaFile")
    public final String valueSchemaFile;

    @JsonCreator
    public TopicSchemaData(
            @JsonProperty("keySchemaFile") String keySchemaFile,
            @JsonProperty("valueSchemaFile")String valueSchemaFile
    ) {
        this.keySchemaFile = keySchemaFile;
        this.valueSchemaFile = valueSchemaFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicSchemaData)) return false;
        TopicSchemaData that = (TopicSchemaData) o;
        return Objects.equals(keySchemaFile, that.keySchemaFile) &&
                Objects.equals(valueSchemaFile, that.valueSchemaFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keySchemaFile, valueSchemaFile);
    }

    @Override
    public String toString() {
        return "TopicSchemaData{" +
                "keySchemaFile='" + keySchemaFile + '\'' +
                ", valueSchemaFile='" + valueSchemaFile + '\'' +
                '}';
    }
}
