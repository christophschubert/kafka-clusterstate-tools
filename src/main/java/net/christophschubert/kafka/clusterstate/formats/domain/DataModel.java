package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

public class DataModel {

    //TODO: add headers - see readme.md

    @JsonProperty("key")
    public final TypeInformation key;

    @JsonProperty("value")
    public final TypeInformation value;

    @JsonCreator
    public DataModel(
            @JsonProperty("key") TypeInformation key,
            @JsonProperty("value") TypeInformation value
    ) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataModel)) return false;
        DataModel dataModel = (DataModel) o;
        return Objects.equals(key, dataModel.key) &&
                Objects.equals(value, dataModel.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "DataModel{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
