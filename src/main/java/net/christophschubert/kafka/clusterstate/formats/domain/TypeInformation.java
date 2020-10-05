package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TypeInformation {
    @JsonProperty("type")
    public final String type;

    @JsonProperty("schemaFile")
    public final String schemaFile;

    @JsonCreator
    public TypeInformation(
            @JsonProperty("type") String type,
            @JsonProperty("schemaFile") String schemaFile
    ) {
        this.type = type;
        this.schemaFile = schemaFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeInformation)) return false;
        TypeInformation that = (TypeInformation) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(schemaFile, that.schemaFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, schemaFile);
    }

    @Override
    public String toString() {
        return "TypeInformation{" +
                "type='" + type + '\'' +
                ", schemaFile='" + schemaFile + '\'' +
                '}';
    }
}
