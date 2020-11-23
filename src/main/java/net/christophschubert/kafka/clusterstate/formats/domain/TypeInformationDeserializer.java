package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.nio.file.Paths;

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
        final var component = node.get("schemaFile");
        if (component != null) {
            final var componentStr = component.textValue();
            schemaFile = basePath == null ? componentStr : Paths.get(basePath, componentStr).toString();
        }

        return new TypeInformation(node.get("type").asText(), schemaFile);
    }
}
