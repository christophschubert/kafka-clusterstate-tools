package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.SubjectNameStrategyName;

import java.util.Objects;


public class TypeInformation {
    public static final String TYPE_KEY = "type";
    public static final String SCHEMA_FILE_KEY = "schemaFile";
    public static final String SUBJECT_NAME_STRATEGY_KEY = "subjectNaming";

    @JsonProperty(TYPE_KEY)
    public final String type;

    @JsonProperty(SCHEMA_FILE_KEY)
    public final String schemaFile;

    @JsonProperty(SUBJECT_NAME_STRATEGY_KEY)
    public final SubjectNameStrategyName subjectNameStrategy;

    @JsonCreator
    public TypeInformation(
            @JsonProperty(TYPE_KEY) String type,
            @JsonProperty(SCHEMA_FILE_KEY) String schemaFile,
            @JsonProperty(SUBJECT_NAME_STRATEGY_KEY) SubjectNameStrategyName subjectNameStrategy
    ) {
        this.type = type;
        this.schemaFile = schemaFile;
        this.subjectNameStrategy = subjectNameStrategy == null ? SubjectNameStrategyName.TOPIC : subjectNameStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeInformation)) return false;
        TypeInformation that = (TypeInformation) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(schemaFile, that.schemaFile) &&
                Objects.equals(subjectNameStrategy, that.subjectNameStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, schemaFile, subjectNameStrategy);
    }

    @Override
    public String toString() {
        return "TypeInformation{" +
                "type='" + type + '\'' +
                ", schemaFile='" + schemaFile + '\'' +
                ", subjectNameStrategy='" + subjectNameStrategy + '\'' +
                '}';
    }
}
