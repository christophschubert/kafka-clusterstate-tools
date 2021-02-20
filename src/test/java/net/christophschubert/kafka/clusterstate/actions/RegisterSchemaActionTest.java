package net.christophschubert.kafka.clusterstate.actions;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.christophschubert.kafka.clusterstate.SerializationInfo;
import net.christophschubert.kafka.clusterstate.SubjectNameStrategyName;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RegisterSchemaActionTest {
    MockSchemaRegistryClient srClient = new MockSchemaRegistryClient();

    final String avroSampleSchema = "{\n" +
            "     \"type\": \"record\",\n" +
            "     \"namespace\": \"com.example\",\n" +
            "     \"name\": \"FullName\",\n" +
            "     \"fields\": [\n" +
            "       { \"name\": \"first\", \"type\": \"string\" },\n" +
            "       { \"name\": \"last\", \"type\": \"string\" }\n" +
            "     ]\n" +
            "}";

    @Test(expected = IllegalArgumentException.class)
    public void loadSchemaTest() throws IOException, RestClientException {
        final var registerSchemaAction = new RegisterSchemaAction("test-topic", "value", new SerializationInfo("Avro", "", SubjectNameStrategyName.RECORD));
        registerSchemaAction.loadAndRegisterSchema(srClient, "unparseable");
    }

    @Test
    public void registerAvroSchema() throws IOException, RestClientException {
        final var recordStrategyInfo = new SerializationInfo("Avro", "src/test/resources/schemas/Fullname.avsc", SubjectNameStrategyName.RECORD);
        final var registerSchemaAction = new RegisterSchemaAction("test-topic", "value", recordStrategyInfo);
        final int id = registerSchemaAction.loadAndRegisterSchema(srClient, avroSampleSchema);

        assertEquals(srClient.parseSchema(AvroSchema.TYPE, avroSampleSchema, Collections.emptyList()).get(), srClient.getSchemaById(id));
    }

}