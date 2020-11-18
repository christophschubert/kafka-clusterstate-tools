package net.christophschubert.kafka.clusterstate.formats.env;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public class Environment {
    @JsonCreator
    public Environment(
            @JsonProperty("clusters") Set<CloudCluster> clusters
    ) {
        this.clusters = clusters;
    }

    @JsonProperty("clusters")
    public final Set<CloudCluster> clusters;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Environment)) return false;
        Environment that = (Environment) o;
        return Objects.equals(clusters, that.clusters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusters);
    }

    @Override
    public String toString() {
        return "Environment{" +
                "clusters=" + clusters +
                '}';
    }
}
