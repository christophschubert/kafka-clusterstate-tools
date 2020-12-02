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
import net.christophschubert.kafka.clusterstate.mds.MdsClient;
import net.christophschubert.kafka.clusterstate.mds.Scope;
import net.christophschubert.kafka.clusterstate.utils.MapTools;
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

    public final static String SCHEMA_REGISTRY_URL_CONFIG = "schema.registry.url";
    public final static String MDS_SERVER_URL_CONFIG = "mds.server.url";
    public final static String MDS_SERVER_USERNAME_CONFIG = "mds.server.login";
    public final static String MDS_SERVER_PASSWORD_CONFIG = "mds.server.password";

    public final static String MDS_SCOPE_FILE_CONFIG = "mds.scope.file";
    public final static String MDS_USE_CLUSTER_REGISTRY_CONFIG = "mds.use.cluster.registry";

    public final Admin adminClient;
    public final SchemaRegistryClient schemaRegistryClient;
    public final MdsClient mdsClient;
    public final Set<Scope> mdsScopes;


    public ClientBundle(Admin adminClient, MdsClient mdsClient, SchemaRegistryClient schemaRegistryClient) {
        this.adminClient = adminClient;
        this.mdsClient = mdsClient;
        this.schemaRegistryClient = schemaRegistryClient;
        this.mdsScopes = Collections.emptySet();
    }

    public ClientBundle(Admin adminClient, MdsClient mdsClient, SchemaRegistryClient schemaRegistryClient, Set<Scope> mdsScopes) {
        this.adminClient = adminClient;
        this.mdsClient = mdsClient;
        this.schemaRegistryClient = schemaRegistryClient;
        this.mdsScopes = mdsScopes;
    }

    public static ClientBundle fromProperties(Properties properties) {
        SchemaRegistryClient srClient = null;

        if (properties.containsKey(SCHEMA_REGISTRY_URL_CONFIG)) {
            final var srBaseUrl = properties.get(SCHEMA_REGISTRY_URL_CONFIG).toString();
            final var restService = new RestService(srBaseUrl);

            final var srConfigPrefix = SchemaRegistryClientConfig.CLIENT_NAMESPACE;
            final var srProperties = MapTools.filterMapKeys(properties,
                    pName -> pName.toString().startsWith(srConfigPrefix),
                    pName -> pName.toString().replace(srConfigPrefix, "")
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

            if (properties.containsKey(MDS_SCOPE_FILE_CONFIG)) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    scopes = mapper.readValue(new File(properties.get(MDS_SCOPE_FILE_CONFIG).toString()), new TypeReference<Set<Scope>>() {});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //TODO: add proper cluster scope to domain file
            } else if (Boolean.parseBoolean(properties.getProperty(MDS_USE_CLUSTER_REGISTRY_CONFIG, "false"))) {
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

        return new ClientBundle(KafkaAdminClient.create(properties), mdsClient, srClient, scopes);
    }

}
