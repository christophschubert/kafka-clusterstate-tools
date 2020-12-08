package net.christophschubert.kafka.clusterstate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class SchemaRegistryState {

    @JsonProperty(COMPATIBILITY_KEY)
    public final String compatibility;

    @JsonProperty(SUBJECTS_KEY)
    public final Map<String, SubjectData> subjects;

    @JsonCreator
    public SchemaRegistryState(
            @JsonProperty(COMPATIBILITY_KEY) String compatibility,
            @JsonProperty(SUBJECTS_KEY) Map<String, SubjectData> subjects
    ) {
        this.compatibility = compatibility;
        this.subjects = subjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchemaRegistryState)) return false;
        SchemaRegistryState that = (SchemaRegistryState) o;
        return Objects.equals(compatibility, that.compatibility) &&
                Objects.equals(subjects, that.subjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatibility, subjects);
    }

    @Override
    public String toString() {
        return "SchemaRegistryState{" +
                "compatibility='" + compatibility + '\'' +
                ", subjects=" + subjects +
                '}';
    }

    public static final String COMPATIBILITY_KEY = "compatibility";
    public static final String SUBJECTS_KEY = "subjects";
}
