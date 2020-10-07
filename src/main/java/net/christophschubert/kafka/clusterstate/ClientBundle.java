package net.christophschubert.kafka.clusterstate;

import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Contains the necessary components to interact with a Kafka cluster.
 */
public class ClientBundle {

    public final Admin adminClient;
    public final SchemaRegistryClient schemaRegistryClient;
    public final File context;


    public ClientBundle(Admin adminClient, SchemaRegistryClient schemaRegistryClient, File context) {
        this.adminClient = adminClient;
        this.context = context;
        this.schemaRegistryClient = schemaRegistryClient;
    }


    public static ClientBundle fromProperties(Properties properties, File context) {
        SchemaRegistryClient srClient = null;
        if (properties.containsKey("schema.registry.url")) {
            final var srBaseUrl = properties.get("schema.registry.url").toString();
            final var restService = new RestService(srBaseUrl);

            srClient = new CachedSchemaRegistryClient(restService, 100,
                    //TODO: make list of loaded providers configurable to allow for custom providers
                    List.of(new AvroSchemaProvider(), new ProtobufSchemaProvider(), new JsonSchemaProvider()),
                    (Map)properties, null);
        }

        return new ClientBundle(KafkaAdminClient.create(properties), srClient, context);
    }

}
