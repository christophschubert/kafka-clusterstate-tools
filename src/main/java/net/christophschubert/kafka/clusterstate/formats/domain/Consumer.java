package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.formats.Helpers;

import java.util.Objects;
import java.util.Set;

public class Consumer extends ProjectSubResource {
    @JsonProperty("principal")
    public final String principal;

    @JsonProperty("groupId")
    public final String groupId;

    @JsonProperty("prefixGroup")
    public boolean prefixGroup;

    @JsonProperty("topics")
    public final Set<String> topics;

    @JsonCreator
    public Consumer(
            @JsonProperty("principal") String principal,
            @JsonProperty("groupId") String groupId,
            @JsonProperty("prefixGroup") boolean prefixGroup,
            @JsonProperty("topics") Set<String> topics
    ) {
        this.principal = principal;
        this.groupId = groupId;
        this.prefixGroup = prefixGroup;
        this.topics = Helpers.emptyForNull(topics);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consumer)) return false;
        Consumer consumer = (Consumer) o;
        return prefixGroup == consumer.prefixGroup &&
                Objects.equals(principal, consumer.principal) &&
                Objects.equals(groupId, consumer.groupId) &&
                Objects.equals(topics, consumer.topics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, groupId, prefixGroup, topics);
    }

    @Override
    public String toString() {
        return "Consumer{" +
                "principal='" + principal + '\'' +
                ", groupId='" + groupId + '\'' +
                ", prefixGroup=" + prefixGroup +
                ", topics=" + topics +
                '}';
    }

    @Override
    public String id() {
        return groupId;
    }
}

