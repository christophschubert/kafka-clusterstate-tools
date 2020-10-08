package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class FeaturesDescription {
    @JsonProperty("features")
    public final Map<String, Boolean> features;

    @JsonProperty("legend")
    public final Map<String, String> legend;

    @JsonCreator
    public FeaturesDescription(
            @JsonProperty("features") Map<String, Boolean> features,
            @JsonProperty("legend") Map<String, String> legend) {
        this.features = features;
        this.legend = legend;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeaturesDescription)) return false;
        FeaturesDescription that = (FeaturesDescription) o;
        return Objects.equals(features, that.features) &&
                Objects.equals(legend, that.legend);
    }

    @Override
    public int hashCode() {
        return Objects.hash(features, legend);
    }

    @Override
    public String toString() {
        return "FeaturesDescription{" +
                "features=" + features +
                ", legend=" + legend +
                '}';
    }
}
