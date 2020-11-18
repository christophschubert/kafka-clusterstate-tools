package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Producer extends ProjectSubResource {
    @JsonProperty("principal")
    public final String principal;

    @JsonProperty("groupId")
    public final String transactionId;

    @JsonProperty("idempotent")
    public final boolean idempotent; //TODO: remove this property: belongs to principal management

    @JsonProperty("topics")
    public final Set<String> topics;

    @JsonCreator
    public Producer(
            @JsonProperty("principal") String principal,
            @JsonProperty("transactionalId") String transactionId,
            @JsonProperty("idempotent") boolean idempotent,
            @JsonProperty("topics") Set<String> topics
    ) {
        this.principal = principal;
        this.transactionId = transactionId;
        this.idempotent = idempotent;
        this.topics = Helpers.emptyForNull(topics);
    }

    public Producer(String principal) {
        this.principal = principal;
        this.transactionId = null;
        this.idempotent = false;
        this.topics = Collections.emptySet();
    }

    @Override
    public String id() {
        return null; // should we use transaction ID here?
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producer)) return false;
        Producer producer = (Producer) o;
        return idempotent == producer.idempotent &&
                Objects.equals(principal, producer.principal) &&
                Objects.equals(transactionId, producer.transactionId) &&
                Objects.equals(topics, producer.topics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, transactionId, idempotent, topics);
    }

    @Override
    public String toString() {
        return "Producer{" +
                "principal='" + principal + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", idempotent=" + idempotent +
                ", topics=" + topics +
                '}';
    }
}
