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

import static org.junit.Assert.assertEquals;

public class ExtractSchemaCompatibilityTest {

    static final String schemaFolder = "src/intTest/resources/schemas";

    @Test(expected = RestClientException.class)
    public void canRegisterSchema() throws IOException, RestClientException {
        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        final var subject = "user-value";
        final CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);
        client.register(subject, new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v1.avsc"))));

        assertEquals(1, client.getAllSubjects().size());
        System.out.println(client.getCompatibility(subject));
    }

    @Test
    public void canUpdateCompatibility() throws IOException, RestClientException {
        // we can read the compatibility of a subject back once we update it

        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                        .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        final var subject = "user-value";
        final CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);
        client.register(subject, new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v1.avsc"))));

        assertEquals(1, client.getAllSubjects().size());

        final var compatibility = "backward";

        client.updateCompatibility(subject, compatibility);

        assertEquals(compatibility.toUpperCase(), client.getCompatibility(subject));
    }

    @Test
    public void canReadCompatibilityWithDefault() throws IOException, RestClientException {
        // we can read the compatibility of a subject back once we update it

        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr-default-compatibility.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                        .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        final var subject = "user-value";
        final CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);
        client.register(subject, new AvroSchema(Files.readString(Path.of(schemaFolder, "user-v1.avsc"))));

        assertEquals(1, client.getAllSubjects().size());

        final var compatibility = "forward";

        client.updateCompatibility(subject, compatibility);

        assertEquals(compatibility.toUpperCase(), client.getCompatibility(subject));
    }

    @Test
    public void nullForSubjectShouldGiveGlobalLevel() throws IOException, RestClientException {
        // we can read the compatibility of a subject back once we update it

        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr-default-compatibility.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                        .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        final var subject = "user-value";
        final CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);

        final var compatibility = "forward";

       // client.updateCompatibility(subject, compatibility);

        assertEquals(compatibility.toUpperCase(), client.getCompatibility(null));
    }

    @Test
    public void backwardIsDefaultGlobalLevel() throws IOException, RestClientException {
        // we can read the compatibility of a subject back once we update it

        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                        .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        final CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);

        final var compatibility = "backward";


        assertEquals(compatibility.toUpperCase(), client.getCompatibility(null));

    }

    @Test
    public void nullForSubjectCanUpdateGlobalLevel() throws IOException, RestClientException {
        // we can read the compatibility of a subject back once we update it

        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/sr/acls-connect-sr-default-compatibility.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort())
                        .withExposedService("schema-registry_1", 8081);
        environment.start();
        final var servicePort = environment.getServicePort("schema-registry_1", 8081);

        final CachedSchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:" + servicePort, 20);

        final var compatibility = "forward_transitive";

        client.updateCompatibility(null, compatibility);

        assertEquals(compatibility.toUpperCase(), client.getCompatibility(null));
        System.out.println(client.getCompatibility(null));
    }
}
