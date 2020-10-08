package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Operation {
    @JsonProperty("resourceType")
    public final String resourceType;

    @JsonProperty("operations")
    public final List<String> operations;

    @JsonCreator
    public Operation(
            @JsonProperty("resourceType") String resourceType,
            @JsonProperty("operations") List<String> operations) {
        this.resourceType = resourceType;
        this.operations = operations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;
        Operation operation = (Operation) o;
        return Objects.equals(resourceType, operation.resourceType) &&
                Objects.equals(operations, operation.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, operations);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "resourceType='" + resourceType + '\'' +
                ", operations=" + operations +
                '}';
    }
}
