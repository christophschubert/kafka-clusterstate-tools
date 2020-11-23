package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class DomainParser {
    public enum Format {JSON, YAML}

    private final TypeInformationDeserializer typeInformationDeserializer = new TypeInformationDeserializer();

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public DomainParser() {
        configureMapper(jsonMapper);
        configureMapper(yamlMapper);
    }

    // register customer deserializers
    private void configureMapper(ObjectMapper mapper) {
        final var module = new SimpleModule().addDeserializer(TypeInformation.class, typeInformationDeserializer);
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static final String DOMAIN_YAML_EXTENSION = ".domy";
    static final String DOMAIN_JSON_EXTENSION = ".domj";

    public Domain loadFromFile(File f) throws IOException {
        final String fileName = f.toString();
        final ObjectMapper mapper = fileName.endsWith(DOMAIN_JSON_EXTENSION) ? jsonMapper : yamlMapper;

        typeInformationDeserializer.basePath(f.getParentFile().getAbsolutePath());

        final Domain domain = mapper.readValue(f, Domain.class);
        domain.updateChildren();
        return domain;
    }

    public String serialize(Domain domain, Format format) throws JsonProcessingException {

        switch (format) {
            case JSON:
                return jsonMapper.writer().writeValueAsString(domain);
            case YAML:
                return yamlMapper.writer().writeValueAsString(domain);
            default:
                return null; //to keep compiler happy.
        }
    }

    public Domain deserialize(String s, Format format, String basePath) throws JsonProcessingException {
        Domain domain = null;
        typeInformationDeserializer.basePath(basePath);
        switch (format) {
            case JSON:
                domain =  jsonMapper.readValue(s, Domain.class);
                break;
            case YAML:
                domain = yamlMapper.readValue(s, Domain.class);
                break;
        }
        // set of parent/child relations between domain/project and project/topic and other resources
        domain.updateChildren();
        return domain;
    }

}
