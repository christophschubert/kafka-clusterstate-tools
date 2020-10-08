package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class RoleDefinition {
    @JsonProperty("name")
    public final String name;

    @JsonProperty("accessPolicy")
    public final AccessPolicy accessPolicy;

    @JsonCreator
    public RoleDefinition(
            @JsonProperty("name") String name,
            @JsonProperty("accessPolicy") AccessPolicy accessPolicy
    ) {
        this.name = name;
        this.accessPolicy = accessPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleDefinition)) return false;
        RoleDefinition that = (RoleDefinition) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(accessPolicy, that.accessPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, accessPolicy);
    }

    @Override
    public String toString() {
        return "RoleDefinition{" +
                "name='" + name + '\'' +
                ", accessPolicy=" + accessPolicy +
                '}';
    }
}
