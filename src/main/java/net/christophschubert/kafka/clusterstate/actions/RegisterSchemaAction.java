package net.christophschubert.kafka.clusterstate.actions;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.SerializationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;


public class RegisterSchemaAction implements Action {

    public static final Map<String, String> tagToProviderType =
            Map.of("Avro", AvroSchema.TYPE,
                    "JSONSchema", JsonSchema.TYPE,
                    "ProtoBuf", ProtobufSchema.TYPE);

    private static final Logger logger = LoggerFactory.getLogger(RegisterSchemaAction.class);

    //TODO: move this to the compiler class and use SubjectNameStrategy
    private final Map<String, String> tagToSuffix = Map.of("key", "-key", "value", "-value");

    private final String topicName;
    private final String keyOrValue;
    private final SerializationInfo serializationInfo;


    public RegisterSchemaAction(String topicName, String keyOrValue, SerializationInfo serializationInfo) {
        this.topicName = topicName;
        this.keyOrValue = keyOrValue;
        this.serializationInfo = serializationInfo;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) {
        final var schemaRegistryClient = bundle.schemaRegistryClient;
        final var basePath = bundle.context.getAbsolutePath();

        final var path = Paths.get(basePath, serializationInfo.schemaFile);
        final var subject = topicName + tagToSuffix.get(keyOrValue);

        try {
            loadAndRegisterSchema(schemaRegistryClient, subject, path, tagToProviderType.getOrDefault(serializationInfo.type, serializationInfo.type));
        } catch (RestClientException | IOException e) {
            logger.error("registering schema for subject " + subject + " failed", e);
        }

        return false;
    }


    int loadAndRegisterSchema(SchemaRegistryClient client, String subject, Path schemaFile, String type) throws IOException, RestClientException {
        final var schemaString = Files.readString(schemaFile);
        final var parsedSchema = client.parseSchema(type, schemaString, Collections.emptyList());
        if (parsedSchema.isPresent()) {
            final var id = client.register(subject, parsedSchema.get());
            logger.info("Registered schema for subject " + subject + " with id: " + id);
            return id;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "RegisterSchemaAction{" +
                "topicName='" + topicName + '\'' +
                ", keyOrValue='" + keyOrValue + '\'' +
                ", serializationInfo=" + serializationInfo +
                '}';
    }
}
