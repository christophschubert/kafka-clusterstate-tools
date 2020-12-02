package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.christophschubert.kafka.clusterstate.SubjectNameStrategyName;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Custom deserializer which rewrites the schema-file paths to absolut paths.
 */
public class TypeInformationDeserializer extends StdDeserializer<TypeInformation> {

    String basePath;

    public void basePath(String basePath) {
        this.basePath = basePath;
    }

    protected TypeInformationDeserializer() {
        super((Class<?>)null);
        this.basePath = null;
    }

    @Override
    public TypeInformation deserialize(JsonParser p, DeserializationContext _context) throws IOException {
        final JsonNode node = p.getCodec().readTree(p);
        String schemaFile = null;
        final var component = node.get(TypeInformation.SCHEMA_FILE_KEY);
        if (component != null) {
            final var componentStr = component.textValue();
            schemaFile = basePath == null ? componentStr : Paths.get(basePath, componentStr).toString();
        }

        SubjectNameStrategyName strategy = SubjectNameStrategyName.TOPIC;
        final var subjectValue = node.get(TypeInformation.SUBJECT_NAME_STRATEGY_KEY);
        if (subjectValue != null) {
            final String subjectStr = subjectValue.textValue().toUpperCase();
            try {
                strategy = SubjectNameStrategyName.valueOf(subjectStr);
            } catch (IllegalArgumentException e) {
                final var subjectNameStrategies = Arrays.asList(SubjectNameStrategyName.values());
                throw new IllegalArgumentException("Unknown subject strategy '" + subjectStr +"'. Possible values are " + subjectNameStrategies, e);
            }
        }

        return new TypeInformation(
                node.get(TypeInformation.TYPE_KEY).asText(),
                schemaFile,
                strategy
        );
    }
}
