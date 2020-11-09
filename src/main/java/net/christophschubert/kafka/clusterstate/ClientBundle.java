package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.json.JsonSchemaProvider;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import net.christophschubert.kafka.clusterstate.mds.ClusterDescription;
import net.christophschubert.kafka.clusterstate.mds.MdsClient;
import net.christophschubert.kafka.clusterstate.mds.Scope;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    public final Set<Scope> mdsScopes;


    public ClientBundle(Admin adminClient, MdsClient mdsClient, SchemaRegistryClient schemaRegistryClient, File context) {
        this.adminClient = adminClient;
        this.mdsClient = mdsClient;
        this.schemaRegistryClient = schemaRegistryClient;
        this.context = context;
        this.mdsScopes = Collections.emptySet();
    }

    public ClientBundle(Admin adminClient, MdsClient mdsClient, SchemaRegistryClient schemaRegistryClient, File context, Set<Scope> mdsScopes) {
        this.adminClient = adminClient;
        this.mdsClient = mdsClient;
        this.schemaRegistryClient = schemaRegistryClient;
        this.context = context;
        this.mdsScopes = mdsScopes;
    }

    public static ClientBundle fromProperties(Properties properties, File context) {
        SchemaRegistryClient srClient = null;
        if (properties.containsKey("schema.registry.url")) {
            final var srBaseUrl = properties.get("schema.registry.url").toString();
            final var restService = new RestService(srBaseUrl);
            final var srProperties = new HashMap<String,Object>();
            properties.entrySet().stream()
                .filter(p -> p.getKey().toString().startsWith(SchemaRegistryClientConfig.CLIENT_NAMESPACE))
                .forEach(p -> srProperties.put(
                    p.getKey().toString().replace(SchemaRegistryClientConfig.CLIENT_NAMESPACE, ""),
                    p.getValue()
                )
            );
            restService.configure(srProperties);
            srClient = new CachedSchemaRegistryClient(restService, 100,
                    //TODO: make list of loaded providers configurable to allow for custom providers
                    List.of(new AvroSchemaProvider(), new ProtobufSchemaProvider(), new JsonSchemaProvider()),
                    (Map)properties, null);
        }
        Set<Scope> scopes = Collections.emptySet();
        MdsClient mdsClient = null;
        if (properties.containsKey(MDS_SERVER_URL_CONFIG)) {
            mdsClient = new MdsClient(
                    properties.get(MDS_SERVER_USERNAME_CONFIG).toString(),
                    properties.get(MDS_SERVER_PASSWORD_CONFIG).toString(),
                    properties.get(MDS_SERVER_URL_CONFIG).toString()
            );

            if (properties.containsKey("mds.scope.file")) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    scopes = mapper.readValue(new File(context.toString(), properties.get("mds.scope.file").toString()), new TypeReference<Set<Scope>>() {});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (Boolean.parseBoolean(properties.getProperty("mds.use.cluster.registry", "false"))) {
                try {
                    System.out.println("Getting scopes from cluster registry");
                    final var clusters = mdsClient.getClusters();
                    scopes = clusters.stream()
                            .map(cd -> new Scope(cd.clusterName, cd.scope.get("clusters")))
                            //.map(Scope::forClusterName)
                            .collect(Collectors.toSet());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ClientBundle(KafkaAdminClient.create(properties), mdsClient, srClient, context, scopes);
    }

}
