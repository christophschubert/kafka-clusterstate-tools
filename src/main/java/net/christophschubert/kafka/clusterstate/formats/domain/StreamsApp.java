package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class StreamsApp extends ProjectSubResource {
    @JsonProperty("applicationId")
    public final String applicationId;

    @JsonProperty("principal")
    public final String principal;

    @JsonCreator
    public StreamsApp(
            @JsonProperty("applicationId") String applicationId,
            @JsonProperty("principal") String principal
    ) {
        this.applicationId = applicationId;
        this.principal = principal;
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
        return Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, principal);
    }

    @Override
    public String toString() {
        return "StreamsApp{" +
                "applicationId='" + applicationId + '\'' +
                ", principal='" + principal + '\'' +
                '}';
    }
}
