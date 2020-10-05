package net.christophschubert.kafka.clusterstate.actions;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.TopicSchemaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;


public class RegisterSchemaAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(RegisterSchemaAction.class);

    //TODO: move this to the compiler class and use SubjectNameStrategy
    private final String VALUE_SUFFIX = "-value";
    private final String KEY_SUFFIX = "-key";

    final String topicName;
    final TopicSchemaData topicSchemaData;

    public RegisterSchemaAction(String topicName, TopicSchemaData topicSchemaData) {
        this.topicName = topicName;
        this.topicSchemaData = topicSchemaData;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) {
        final var schemaRegistryClient = bundle.schemaRegistryClient;
        final var basePath = bundle.context.getAbsolutePath();

        if (topicSchemaData.valueSchemaFile != null) {
            final var subject = topicName + VALUE_SUFFIX;
            final var path = Paths.get(basePath, topicSchemaData.valueSchemaFile);
            handleSubject(schemaRegistryClient, subject, path);
        }
        if (topicSchemaData.keySchemaFile != null) {
            final var subject = topicName + KEY_SUFFIX;
            final var path = Paths.get(basePath, topicSchemaData.keySchemaFile);
            handleSubject(schemaRegistryClient, subject, path);
        }
        return false;
    }

    void handleSubject(SchemaRegistryClient client, String subject, Path schemaFile) {
        try {
            loadAndRegisterSchema(client, subject, schemaFile);
        } catch (RestClientException | IOException e) {
            logger.error("registering schema for subject " + subject + " failed", e);
        }
    }

    void loadAndRegisterSchema(SchemaRegistryClient client, String subject, Path schemaFile) throws IOException, RestClientException {
        final var schemaString = Files.readString(schemaFile);
        final var parsedSchema = client.parseSchema(AvroSchema.TYPE, schemaString, Collections.emptyList());
        if (parsedSchema.isPresent()) {
            final var id = client.register(subject, parsedSchema.get());
            logger.info("Registered schema for subject " + subject + " with id: " + id);
        }
    }

    @Override
    public String toString() {
        return "RegisterSchemaAction{" +
                "topicName='" + topicName + '\'' +
                ", topicSchemaData=" + topicSchemaData +
                '}';
    }
}
