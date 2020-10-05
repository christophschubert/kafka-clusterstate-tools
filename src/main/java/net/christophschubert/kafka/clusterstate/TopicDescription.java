package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class TopicDescription {
    private final String name;
    private final Map<String, String> configs;
    private final TopicSchemaData schemaData;

    @JsonGetter
    public String name() {
        return name;
    }
    @JsonGetter
    public Map<String, String> configs() {
        return configs;
    }

    @JsonGetter("schemaData")
    public TopicSchemaData schemaData() { return schemaData; }

    @JsonCreator
    public TopicDescription(
            @JsonProperty("name") String name,
            @JsonProperty("configs") Map<String, String> configs,
            @JsonProperty("schemaData") TopicSchemaData schemaData
    ) {
        this.name = name;
        this.configs = configs;
        this.schemaData = schemaData;
    }

    @Override
    public String toString() {
        return "TopicDescription{" +
                "name='" + name + '\'' +
                ", configs=" + configs +
                ", schemaData=" + schemaData +
                '}';
    }


    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        TopicDescription td = new TopicDescription("hello", Map.of("k1", "v1", "k2", "v2"), new TopicSchemaData("keyf", "valuef"));


        final String ser = mapper.writer().writeValueAsString(td);
        System.out.println(ser);
        final var topicDescription = mapper.readValue(ser, TopicDescription.class);
        System.out.println(topicDescription);
    }
}
