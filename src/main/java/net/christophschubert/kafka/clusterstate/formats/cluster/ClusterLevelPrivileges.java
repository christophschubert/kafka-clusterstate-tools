package net.christophschubert.kafka.clusterstate.formats.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Set;

import static net.christophschubert.kafka.clusterstate.formats.Helpers.emptyForNull;

public class ClusterLevelPrivileges {
    @JsonProperty("acls")
    public final ClusterLevelAcls acls;

    @JsonProperty("namespaces")
    public final Set<Namespace> namespaces;

    public ClusterLevelPrivileges(ClusterLevelAcls acls) {
        this(acls, Collections.emptySet());
    }

    @JsonCreator
    public ClusterLevelPrivileges(
            @JsonProperty("acls") ClusterLevelAcls acls,
            @JsonProperty("namespaces") Set<Namespace> namespaces
    ) {
        this.acls = acls;
        this.namespaces = emptyForNull(namespaces);
    }

}
