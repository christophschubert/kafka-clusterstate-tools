package net.christophschubert.kafka.clusterstate.mds;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class ClusterRegistry {
    private final MdsClient client;

    private final Map<String, ClusterDescription> clusters = new HashMap<>();

    public ClusterRegistry(MdsClient client) {
        this.client = client;
        load();
    }

    public Optional<String> getKafkaNameForId(String clusterId) {
        return clusters.values().stream().filter(cd -> {
            final var cs = cd.scope.get("clusters");
            return cs.connectCluster == null && cs.ksqlCluster==null &&
                    cs.schemaRegistryCluster==null &&cs.kafkaCluster.equals(clusterId);
        }).map(cd -> cd.clusterName).findAny();
    }

    private void load() {
        try {
            client.getClusters().
                    forEach(cd -> clusters.put(cd.clusterName, cd));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
