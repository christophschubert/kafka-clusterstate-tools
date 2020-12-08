package net.christophschubert.kafka.clusterstate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class SubjectData {
    @JsonProperty(COMPATIBILITY_KEY)
    public final String compatibility;

    @JsonProperty(VERSION_KEY)
    public final Map<Integer, SchemaData> versions;

    //TODO: add ACLs/rolebindings for subject

    @JsonCreator
    public SubjectData(
            @JsonProperty(COMPATIBILITY_KEY) String compatibility,
            @JsonProperty(VERSION_KEY) Map<Integer, SchemaData> versions
    ) {
        this.compatibility = compatibility;
        this.versions = versions;
    }

    public final static String COMPATIBILITY_KEY = "compatibility";
    public final static String VERSION_KEY = "versions";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectData)) return false;
        SubjectData that = (SubjectData) o;
        return Objects.equals(compatibility, that.compatibility) &&
                Objects.equals(versions, that.versions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compatibility, versions);
    }

    @Override
    public String toString() {
        return "SubjectData{" +
                "compatibility='" + compatibility + '\'' +
                ", versions=" + versions +
                '}';
    }
}
