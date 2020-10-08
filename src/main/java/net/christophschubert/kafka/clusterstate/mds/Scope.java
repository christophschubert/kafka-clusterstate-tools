package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.ClusterState;

import java.util.Objects;

public class Scope {
    @JsonProperty("clusterName")
    public final String clusterName;

    @JsonProperty("clusters")
    public final Clusters clusters;

    @JsonCreator
    public Scope(
            @JsonProperty("clusterName") String clusterName,
            @JsonProperty("clusters") Clusters clusters
    ) {
        this.clusterName = clusterName;
        this.clusters = clusters;
    }

    public static Scope forClusterId(String clusterId) {
        return new Scope(null,
                new Clusters(clusterId, null, null, null)
        );
    }

    public static Scope forConnectCluster(String kafkaClusterId, String connectClusterId) {
        return new Scope (null, new Clusters(kafkaClusterId, connectClusterId, null, null));
    }

    public static Scope forKsqldbCluster(String kafkaClusterId, String ksqlDBClusterId) {
        return new Scope (null, new Clusters(kafkaClusterId, null, ksqlDBClusterId, null));
    }

    public static Scope forSchemaRegistryCluster(String kafkaClusterId, String schemaRegistryClusterId) {
        return new Scope (null, new Clusters(kafkaClusterId, null, null, schemaRegistryClusterId));
    }

    public static Scope forClusterName(String clusterName) {
        return new Scope (clusterName, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scope)) return false;
        Scope scope = (Scope) o;
        return Objects.equals(clusterName, scope.clusterName) &&
                Objects.equals(clusters, scope.clusters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterName, clusters);
    }

    @Override
    public String toString() {
        return "Scope{" +
                "clusterName='" + clusterName + '\'' +
                ", clusters=" + clusters +
                '}';
    }

}
