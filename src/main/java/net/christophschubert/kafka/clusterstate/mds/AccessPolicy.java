package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class AccessPolicy {
    @JsonProperty("scopeType")
    public final String scopeType;

    @JsonProperty("allowedOperations")
    public final List<Operation> allowedOperations;

    @JsonCreator
    public AccessPolicy(
            @JsonProperty("scopeType") String scopeType,
            @JsonProperty("allowedOperations") List<Operation> allowedOperations) {
        this.scopeType = scopeType;
        this.allowedOperations = allowedOperations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessPolicy)) return false;
        AccessPolicy that = (AccessPolicy) o;
        return Objects.equals(scopeType, that.scopeType) &&
                Objects.equals(allowedOperations, that.allowedOperations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeType, allowedOperations);
    }

    @Override
    public String toString() {
        return "AccessPolicy{" +
                "scopeType='" + scopeType + '\'' +
                ", allowedOperations=" + allowedOperations +
                '}';
    }
}
