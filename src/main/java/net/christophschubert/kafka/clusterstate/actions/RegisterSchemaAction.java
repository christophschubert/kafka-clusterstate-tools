package net.christophschubert.kafka.clusterstate.actions;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.SerializationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

// TODO: simplify code and write proper tests
public class RegisterSchemaAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(RegisterSchemaAction.class);

    public static final Map<String, String> tagToProviderType =
            Map.of("Avro", AvroSchema.TYPE,
                    "JSONSchema", JsonSchema.TYPE,
                    "ProtoBuf", ProtobufSchema.TYPE);

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

        try {
            final var schemaString = Files.readString(Path.of(serializationInfo.schemaFile));
            loadAndRegisterSchema(schemaRegistryClient, schemaString);
        } catch (RestClientException | IOException e) {
            logger.error("registering schema for subject  failed", e);
        }

        return false;
    }


    int loadAndRegisterSchema(SchemaRegistryClient client, String schemaString) throws IOException, RestClientException {

        final String type = tagToProviderType.getOrDefault(serializationInfo.type, serializationInfo.type);

        logger.info("Try to register schema : {##############"  );
        logger.info( schemaString );
        logger.info("########################################}" );

        final var parsedSchema = client.parseSchema(type, schemaString, Collections.emptyList());
        if (parsedSchema.isPresent()) {
            final SubjectNameStrategy strategy = serializationInfo.subjectStrategy.strategy;
            final var subjectName = strategy.subjectName(topicName, keyOrValue.equals("key"), parsedSchema.get());

            final var id = client.register(subjectName, parsedSchema.get());
            logger.info("Registered schema for subject " + subjectName + " with id: " + id);
            return id;
        } else {
            throw new IllegalArgumentException("Could not parse Schema '" + parsedSchema + "' as " + type);
        }
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
