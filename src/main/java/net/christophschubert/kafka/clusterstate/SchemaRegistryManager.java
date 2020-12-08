package net.christophschubert.kafka.clusterstate;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema;
import net.christophschubert.kafka.clusterstate.model.SchemaData;
import net.christophschubert.kafka.clusterstate.model.SchemaRegistryState;
import net.christophschubert.kafka.clusterstate.model.SubjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class SchemaRegistryManager {
    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistryManager.class);

    interface SchemaMetadataHandler {
        SchemaData handle(SchemaMetadata data);
    }

    protected SchemaRegistryState extractData(ClientBundle bundle, SchemaMetadataHandler handler) throws IOException, RestClientException {
        final Map<String, SubjectData> subjects = new HashMap<>();

        final var client = bundle.schemaRegistryClient;
        final var globalCompatibility = getCompatibility(client, null);
        final var subjectNames = client.getAllSubjects();
        for (String subjectName : subjectNames) {
            logger.info("Getting data for subject {}", subjectName);
            final String compatibility = getCompatibility(client, subjectName);
            final Map<Integer, SchemaData> schemas = new HashMap<>();
            for (Integer version : client.getAllVersions(subjectName)) {
                schemas.put(version, handler.handle(client.getSchemaMetadata(subjectName, version)));
            }
            subjects.put(subjectName, new SubjectData(compatibility, schemas));
        }

        return new SchemaRegistryState(globalCompatibility, subjects);
    }

    //includes schema strings directly in structure.
    public SchemaRegistryState build(ClientBundle bundle) throws IOException, RestClientException {
        return extractData(bundle, SchemaData::fromMetadata);
    }

    public SchemaRegistryState extractState(ClientBundle bundle, Path schemaPath) throws IOException, RestClientException {

        final var fileExtensions = Map.of(
                AvroSchema.TYPE, "avsc",
                JsonSchema.TYPE, "json",
                ProtobufSchema.TYPE,  "proto"
            );

        return extractData(bundle, metadata -> {
            final var extension = fileExtensions.getOrDefault(metadata.getSchemaType(), "schema");
            final String filename = String.format("schema-%d.%s", metadata.getId(), extension);
            //TODO: rename
            final Path filePath = schemaPath.resolve(filename);
            logger.info("Writing to {}", filePath);
            try {
                Files.writeString(filePath, metadata.getSchema(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                logger.error("cannot write to file {}", filePath);
            }
            //TODO: add code to handle schema references
            return new SchemaData(metadata.getId(), metadata.getSchemaType(), null, filename, metadata.getVersion(), null);
        });
    }

    /**
     *
     * @param client
     * @param subjectName name of the subject or null for global compatibility setting
     * @return compatibility setting or null if no equal to cluster default.
     * @throws IOException
     * @throws RestClientException
     */
    String getCompatibility(SchemaRegistryClient client, String subjectName) throws IOException, RestClientException {
        try{
            return client.getCompatibility(subjectName);
        } catch (RestClientException e) {
            if (e.getErrorCode() == 40401) {
                logger.debug("got null compact for subject {}", subjectName);
                return null;
            } else {
                throw e;
            }
        }
    }
}
