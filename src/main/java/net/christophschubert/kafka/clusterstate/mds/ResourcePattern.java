package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

//TODO:
public class ResourcePattern {
    @JsonProperty("resourceType")
    public final String resourceType;

    @JsonProperty("name")
    public final String name;

    @JsonProperty("patternType")
    public final String patternType;

    @JsonCreator
    public ResourcePattern(
            @JsonProperty("resourceType") String resourceType,
            @JsonProperty("name") String name,
            @JsonProperty("patternType") String patternType
    ) {
        this.resourceType = resourceType;
        this.name = name;
        this.patternType = patternType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourcePattern)) return false;
        ResourcePattern that = (ResourcePattern) o;
        return Objects.equals(resourceType, that.resourceType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(patternType, that.patternType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, name, patternType);
    }

    @Override
    public String toString() {
        return "ResourcePattern{" +
                "resourceType='" + resourceType + '\'' +
                ", name='" + name + '\'' +
                ", patternType='" + patternType + '\'' +
                '}';
    }
}
