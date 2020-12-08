package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class SerializationInfo {

    @JsonProperty("type")
    public final String type;

    @JsonProperty("schemaFile")
    public final String schemaFile;

    @JsonProperty("subjectStrategy")
    public final SubjectNameStrategyName subjectStrategy;

    @JsonCreator
    public SerializationInfo(
            @JsonProperty("type") String type,
            @JsonProperty("schemaFile") String schemaFile,
            @JsonProperty("subjectStrategy") SubjectNameStrategyName subjectStrategy
    ) {
        this.type = type;
        this.schemaFile = schemaFile;
        this.subjectStrategy = subjectStrategy == null ? SubjectNameStrategyName.TOPIC : subjectStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializationInfo)) return false;
        SerializationInfo that = (SerializationInfo) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(schemaFile, that.schemaFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, schemaFile);
    }

    @Override
    public String toString() {
        return "SerializationInfo{" +
                "type='" + type + '\'' +
                ", schemaFile='" + schemaFile + '\'' +
                '}';
    }
}
