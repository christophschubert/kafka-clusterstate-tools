package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Integration tests for MdsClient.
 * <p>
 * Supposed to run against the rbac example from https://github.com/Dabz/kafka-security-playbook/tree/master/rbac
 * with CP Version set to 6.0.0
 */
public class MdsClientIT {

    public static void printFeatureSummary(FeaturesDescription features) {
        System.out.println("Enabled MDS features:");
        features.features.forEach((name, valid) -> {
            if (valid) {
                System.out.println(" - " + features.legend.get(name));
            }
        });
    }

    public static void main(String[] args) throws Exception {

        final var client = new MdsClient("alice", "alice-secret", "http://localhost:8090");

        // only 1 cluster => metadata cluster = Kafka cluster!
        final String kafkaClusterId = client.metadataClusterId();
        System.out.println("CloudCluster Id: " + kafkaClusterId);
        final FeaturesDescription features = client.features();
        printFeatureSummary(features);

       // System.out.println(client.getClusters());

//        System.out.println(client.roles());
//        System.out.println(client.roles("ClusterAdmin"));
//        System.out.println(client.roleNames());
//        final Scope kafkaScope = Scope.forClusterId(kafkaClusterId);
//
//        System.out.println(client.roleNamesForPrincipal("User:charlie", kafkaScope));
////        assertEquals(Collections.singleton("SystemAdmin"), client.roleNamesForPrincipal("User:charlie", kafkaScope));
//
//        client.bindClusterRole("User:charlie", "ClusterAdmin", kafkaScope);
////        assertEquals(Set.of("SystemAdmin", "ClusterAdmin"), client.roleNamesForPrincipal("User:charlie", kafkaScope));
//
//        client.unbindClusterRole("User:charlie", "ClusterAdmin", kafkaScope);
////        assertEquals(Set.of("SystemAdmin"), client.roleNamesForPrincipal("User:charlie", kafkaScope));
//
//
////        client.addBinding("User:fred", "ResourceOwner", kafkaScope,
////                List.of(new ResourcePattern("Topic", "test-2", "PREFIXED")));
//        client.removeBinding("User:fred", "ResourceOwner", kafkaScope,
//                List.of(new ResourcePattern("Topic", "test-2", "PREFIXED")));
//
//
//
//        System.out.println(client.bindingsForPrincipal("User:fred", kafkaScope));

//        final var roles = client.roles();
//
        final var objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        System.out.println(objectWriter.writeValueAsString(client.getClusters()));
    }

}