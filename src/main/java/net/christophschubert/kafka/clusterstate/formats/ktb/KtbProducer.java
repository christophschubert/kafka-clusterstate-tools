package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class KtbProducer implements KtbClient{
    String principal;

    @JsonCreator
    public KtbProducer(
            @JsonProperty("principal") String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }


    @Override
    public String toString() {
        return "KtbConsumer{" +
                "principal='" + principal + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KtbProducer)) return false;
        KtbProducer that = (KtbProducer) o;
        return Objects.equals(principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal);
    }
}
