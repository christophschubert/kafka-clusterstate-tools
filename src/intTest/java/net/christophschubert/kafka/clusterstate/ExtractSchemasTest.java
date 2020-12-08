package net.christophschubert.kafka.clusterstate;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ExtractSchemasTest {

    static final String schemaFolder = "src/intTest/resources/schemas";

    @Test
    public void test() throws IOException, RestClientException {
        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);

        client.register("test-value", new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v1.avsc"))));
        client.register("test-value", new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v2.avsc"))));
        // registering twice should yield same ID
        client.register("test-value", new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v2.avsc"))));


        client.register("user-value", new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v1.avsc"))));
        client.register("xxx-yyy-zzz", new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v1.avsc"))));

        client.updateCompatibility("xxx-yyy-zzz", "forward");

        assertEquals(3, client.getAllSubjects().size());

        System.out.println(client.getAllSubjects());
        ClientBundle bundle = new ClientBundle(null, null, client);
        System.out.println(new SchemaRegistryManager().build(bundle));

        System.out.println(new SchemaRegistryManager().extractState(bundle, Paths.get("/Users/cschubert/schemas")));
    }
}
