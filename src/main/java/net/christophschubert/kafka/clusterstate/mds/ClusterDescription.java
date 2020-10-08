package net.christophschubert.kafka.clusterstate.mds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClusterDescription {
    @JsonProperty("clusterName")
    public final String clusterName;

    @JsonProperty("scope")
    public final Map<String, Clusters> scope;

    @JsonProperty("hosts")
    public final Set<HostPort> hosts;

    @JsonProperty("protocol")
    public final String protocol;

    @JsonCreator
    public ClusterDescription(
            @JsonProperty("clusterName") String clusterName,
            @JsonProperty("scope") Map<String, Clusters> scope,
            @JsonProperty("hosts") Set<HostPort> hosts,
            @JsonProperty("protocol") String protocol
    ) {
        this.clusterName = clusterName;
        this.scope = scope;
        this.hosts = hosts;
        this.protocol = protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusterDescription)) return false;
        ClusterDescription that = (ClusterDescription) o;
        return Objects.equals(clusterName, that.clusterName) &&
                Objects.equals(scope, that.scope) &&
                Objects.equals(hosts, that.hosts) &&
                Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterName, scope, hosts, protocol);
    }

    @Override
    public String toString() {
        return "ClusterDescription{" +
                "clusterName='" + clusterName + '\'' +
                ", scope=" + scope +
                ", hosts=" + hosts +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
