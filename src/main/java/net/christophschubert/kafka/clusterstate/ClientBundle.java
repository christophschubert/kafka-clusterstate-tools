package net.christophschubert.kafka.clusterstate;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

// contains the necessary components to interact with a Kafka cluster
public class ClientBundle {

    public final Admin adminClient;

    public ClientBundle(Admin adminClient) {
        this.adminClient = adminClient;
    }

    public static ClientBundle fromProperties(Properties properties) {
        return new ClientBundle(KafkaAdminClient.create(properties));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final Admin adminClient = KafkaAdminClient.create(props);
        ClientBundle bundle = new ClientBundle(adminClient);
        final ClusterState build = ClusterStateManager.build(bundle);
        System.out.println(build);
    }
}
