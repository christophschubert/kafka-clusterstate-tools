package net.christophschubert.kafka.clusterstate;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

// contains the necessary components to interact with a Kafka cluster
public class ClientBundle {

    public final Admin adminClient;
    public final SchemaRegistryClient schemaRegistryClient;
    public final File context;


    public ClientBundle(Admin adminClient, File context) {
        this.adminClient = adminClient;
        this.context = context;
        // TODO: remove this!
        this.schemaRegistryClient = new CachedSchemaRegistryClient("http://localhost:8081", 10);
}


    public static ClientBundle fromProperties(Properties properties, File context) {

        return new ClientBundle(KafkaAdminClient.create(properties), context);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException, RestClientException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");


        final Admin adminClient = KafkaAdminClient.create(props);
        ClientBundle bundle = new ClientBundle(adminClient, new File("."));
        final ClusterState build = ClusterStateManager.build(bundle);
        System.out.println(build);


    }
}
