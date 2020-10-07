package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ResourceResponse {
    @JsonProperty("scope")
    public final Scope scope;
    @JsonProperty("resourcePatterns")
    public final List<ResourcePattern> resourcePatterns;

    @JsonCreator
    public ResourceResponse(
           @JsonProperty("scope") Scope scope,
           @JsonProperty("resourcePatterns") List<ResourcePattern> resourcePatterns
    ) {
        this.scope = scope;
        this.resourcePatterns = resourcePatterns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceResponse)) return false;
        ResourceResponse that = (ResourceResponse) o;
        return Objects.equals(scope, that.scope) &&
                Objects.equals(resourcePatterns, that.resourcePatterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, resourcePatterns);
    }

    @Override
    public String toString() {
        return "ResourceResponse{" +
                "scope=" + scope +
                ", resourcePatterns=" + resourcePatterns +
                '}';
    }
}
