package net.christophschubert.kafka.clusterstate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Objects;
import java.util.Set;

public class SchemaData {

    @JsonProperty(ID_KEY)
    public final int id;

    @JsonProperty(TYPE_KEY)
    public final String type;

    @JsonProperty(SCHEMA_KEY)
    public final String schema;

    @JsonProperty(SCHEMA_FILE_KEY)
    public final String schemaFile;

    @JsonProperty(VERSION_KEY)
    public final int version;

    @JsonProperty(SCHEMA_REFERENCES_KEY)
    public final Set<SchemaReferenceData> schemaReferences;

    public SchemaData(
            @JsonProperty(ID_KEY) int id,
            @JsonProperty(TYPE_KEY) String type,
            @JsonProperty(SCHEMA_KEY) String schema,
            @JsonProperty(SCHEMA_FILE_KEY) String schemaFile,
            @JsonProperty(VERSION_KEY) int version,
            @JsonProperty(SCHEMA_REFERENCES_KEY) Set<SchemaReferenceData> schemaReferences
    ) {
        this.id = id;
        this.type = type;
        this.schema = schema;
        this.schemaFile = schemaFile;
        this.version = version;
        this.schemaReferences = Helpers.emptyForNull(schemaReferences);
    }

    public static SchemaData fromMetadata(SchemaMetadata metadata) {
        return new SchemaData(
                metadata.getId(),
                metadata.getSchemaType(),
                metadata.getSchema(),
                null,
                metadata.getVersion(),
                null //TODO: add proper conversion code
        );
    }

    @Override
    public String toString() {
        return "SchemaData{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", schema='" + schema + '\'' +
                ", schemaFile='" + schemaFile + '\'' +
                ", version=" + version +
                ", schemaReferences=" + schemaReferences +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchemaData)) return false;
        SchemaData that = (SchemaData) o;
        return id == that.id &&
                version == that.version &&
                Objects.equals(type, that.type) &&
                Objects.equals(schema, that.schema) &&
                Objects.equals(schemaFile, that.schemaFile) &&
                Objects.equals(schemaReferences, that.schemaReferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, schema, schemaFile, version, schemaReferences);
    }

    public final static String ID_KEY = "id";
    public final static String TYPE_KEY = "type";
    public final static String SCHEMA_KEY = "schema";
    public final static String SCHEMA_FILE_KEY = "schemaFile";
    public final static String VERSION_KEY = "version";
    public final static String SCHEMA_REFERENCES_KEY = "schemaReferences";
}
