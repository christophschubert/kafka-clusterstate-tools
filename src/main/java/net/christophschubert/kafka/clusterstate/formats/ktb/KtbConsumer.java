package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class KtbConsumer implements KtbClient{
    String principal;
    String groupId;

    @JsonCreator
    public KtbConsumer(
            @JsonProperty("principal") String principal,
            @JsonProperty("groupId") String groupId) {
        this.principal = principal;
        this.groupId = groupId;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KtbConsumer)) return false;
        KtbConsumer that = (KtbConsumer) o;
        return Objects.equals(principal, that.principal) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, groupId);
    }

    @Override
    public String toString() {
        return "KtbConsumer{" +
                "principal='" + principal + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
