package net.christophschubert.kafka.clusterstate;

import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import net.christophschubert.kafka.clusterstate.mds.MdsClient;
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

    public final static String MDS_SERVER_URL_CONFIG = "mds.server.url";
    public final static String MDS_SERVER_USERNAME_CONFIG = "mds.server.login";
    public final static String MDS_SERVER_PASSWORD_CONFIG = "mds.server.password";

    public final Admin adminClient;
    public final SchemaRegistryClient schemaRegistryClient;
    public final MdsClient mdsClient;
    public final File context;


    public ClientBundle(Admin adminClient, MdsClient mdsClient, SchemaRegistryClient schemaRegistryClient, File context) {
        this.adminClient = adminClient;
        this.mdsClient = mdsClient;
        this.schemaRegistryClient = schemaRegistryClient;
        this.context = context;
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

        MdsClient mdsClient = null;
        if (properties.containsKey(MDS_SERVER_URL_CONFIG)) {
            mdsClient = new MdsClient(
                    properties.get(MDS_SERVER_USERNAME_CONFIG).toString(),
                    properties.get(MDS_SERVER_PASSWORD_CONFIG).toString(),
                    properties.get(MDS_SERVER_URL_CONFIG).toString()
            );
        }

        return new ClientBundle(KafkaAdminClient.create(properties), mdsClient, srClient, context);
    }

}
