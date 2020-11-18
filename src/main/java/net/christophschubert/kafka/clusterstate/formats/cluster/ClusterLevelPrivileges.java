package net.christophschubert.kafka.clusterstate.formats.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClusterLevelPrivileges {
    @JsonProperty("acls")
    public final ClusterLevelAcls acls;

    @JsonCreator
    public ClusterLevelPrivileges(
            @JsonProperty("acls") ClusterLevelAcls acls
    ) {

        this.acls = acls;
    }
}
