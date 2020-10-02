package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DataModel {
    @JsonProperty("keySchemaFile")
    public final String keySchemaFile;

    @JsonProperty("valueSchemaFile")
    public final String valueSchemaFile;

    @JsonCreator
    public DataModel(
            @JsonProperty("keySchemaFile") String keySchemaFile,
            @JsonProperty("valueSchemaFile") String valueSchemaFile
    ) {
        this.keySchemaFile = keySchemaFile;
        this.valueSchemaFile = valueSchemaFile;
    }

    @Override
    public String toString() {
        return "DataModel{" +
                "keySchemaFile='" + keySchemaFile + '\'' +
                ", valueSchemaFile='" + valueSchemaFile + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataModel)) return false;
        DataModel dataModel = (DataModel) o;
        return Objects.equals(keySchemaFile, dataModel.keySchemaFile) &&
                Objects.equals(valueSchemaFile, dataModel.valueSchemaFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keySchemaFile, valueSchemaFile);
    }
}
