package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Objects;
import java.util.Set;

public class StreamsApp extends ProjectSubResource {
    @JsonProperty("applicationId")
    public final String applicationId;

    @JsonProperty("principal")
    public final String principal;

    @JsonProperty("prefixApplicationId")
    public final boolean prefixApplicationId;

    @JsonProperty("inputTopics")
    public final Set<String> inputTopics;

    @JsonProperty("outputTopics")
    public final Set<String> outputTopics;

    @JsonCreator
    public StreamsApp(
            @JsonProperty("applicationId") String applicationId,
            @JsonProperty("principal") String principal,
            @JsonProperty("prefixApplicationId") boolean prefixApplicationId,
            @JsonProperty("inputTopics") Set<String> inputTopics,
            @JsonProperty("outputTopics") Set<String> outputTopics
    ) {
        this.applicationId = applicationId;
        this.principal = principal;
        this.prefixApplicationId = prefixApplicationId;
        this.inputTopics = Helpers.emptyForNull(inputTopics);
        this.outputTopics = Helpers.emptyForNull(outputTopics);
    }

    @Override
    public String id() {
        return applicationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StreamsApp)) return false;
        StreamsApp that = (StreamsApp) o;
        return prefixApplicationId == that.prefixApplicationId &&
                Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(principal, that.principal) &&
                Objects.equals(inputTopics, that.inputTopics) &&
                Objects.equals(outputTopics, that.outputTopics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, principal, prefixApplicationId, inputTopics, outputTopics);
    }

    @Override
    public String toString() {
        return "StreamsApp{" +
                "applicationId='" + applicationId + '\'' +
                ", principal='" + principal + '\'' +
                ", prefixApplicationId=" + prefixApplicationId +
                ", inputTopics=" + inputTopics +
                ", outputTopics=" + outputTopics +
                '}';
    }
}
