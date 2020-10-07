package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Clusters {
    static private final String KAFKA_CLUSTER_KEY = "kafka-cluster";
    static private final String CONNECT_CLUSTER_KEY = "connect-cluster";
    static private final String KSQL_CLUSTER_KEY = "ksql-cluster";
    static private final String SCHEMA_REGISTRY_CLUSTER = "schema-registry-cluster";

    @JsonProperty(KAFKA_CLUSTER_KEY)
    public final String kafkaCluster;
    @JsonProperty(CONNECT_CLUSTER_KEY)
    public final String connectCluster;
    @JsonProperty(KSQL_CLUSTER_KEY)
    public final String ksqlCluster;
    @JsonProperty(SCHEMA_REGISTRY_CLUSTER)
    public final String schemaRegistryCluster;

    @JsonCreator
    public Clusters(
            @JsonProperty(KAFKA_CLUSTER_KEY) String kafkaCluster,
            @JsonProperty(CONNECT_CLUSTER_KEY) String connectCluster,
            @JsonProperty(KSQL_CLUSTER_KEY) String ksqlCluster,
            @JsonProperty(SCHEMA_REGISTRY_CLUSTER) String schemaRegistryCluster
    ) {
        this.kafkaCluster = kafkaCluster;
        this.connectCluster = connectCluster;
        this.ksqlCluster = ksqlCluster;
        this.schemaRegistryCluster = schemaRegistryCluster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Clusters)) return false;
        Clusters clusters = (Clusters) o;
        return Objects.equals(kafkaCluster, clusters.kafkaCluster) &&
                Objects.equals(connectCluster, clusters.connectCluster) &&
                Objects.equals(ksqlCluster, clusters.ksqlCluster) &&
                Objects.equals(schemaRegistryCluster, clusters.schemaRegistryCluster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kafkaCluster, connectCluster, ksqlCluster, schemaRegistryCluster);
    }

    @Override
    public String toString() {
        return "Clusters{" +
                "kafkaCluster='" + kafkaCluster + '\'' +
                ", connectCluster='" + connectCluster + '\'' +
                ", ksqlCluster='" + ksqlCluster + '\'' +
                ", schemaRegistryCluster='" + schemaRegistryCluster + '\'' +
                '}';
    }
}
