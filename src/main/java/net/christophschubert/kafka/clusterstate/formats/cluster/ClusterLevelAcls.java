package net.christophschubert.kafka.clusterstate.formats.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.acl.AclOperation;

import java.util.Objects;
import java.util.Set;
import static net.christophschubert.kafka.clusterstate.formats.Helpers.emptyForNull;

public class ClusterLevelAcls {
    //entries for cluster-scoped ACLs
    @JsonProperty("alterPrincipals")
    public final Set<String> alterPrincipals;

    @JsonProperty("alterConfigsPrincipals")
    public final Set<String> alterConfigsPrincipals;

    @JsonProperty("clusterActionPrincipals")
    public final Set<String> clusterActionPrincipals;

    @JsonProperty("createPrincipals")
    public final Set<String> createPrincipals;

    @JsonProperty("describePrincipals")
    public final Set<String> describePrincipals;

    @JsonProperty("describeConfigsPrincipals")
    public final Set<String> describeConfigsPrincipals;

    @JsonProperty("idempotentWritePrincipals")
    public final Set<String> idempotentWritePrincipals;

    @JsonCreator
    public ClusterLevelAcls(
            @JsonProperty("alterPrincipals") Set<String> alterPrincipals,
            @JsonProperty("alterConfigsPrincipals") Set<String> alterConfigsPrincipals,
            @JsonProperty("clusterActionPrincipals")Set<String> clusterActionPrincipals,
            @JsonProperty("createPrincipals") Set<String> createPrincipals,
            @JsonProperty("describePrincipals") Set<String> describePrincipals,
            @JsonProperty("describeConfigsPrincipals") Set<String> describeConfigsPrincipals,
            @JsonProperty("idempotentWritePrincipals") Set<String> idempotentWritePrincipals
    ) {
        this.alterPrincipals = emptyForNull(alterPrincipals);
        this.alterConfigsPrincipals = emptyForNull(alterConfigsPrincipals);
        this.clusterActionPrincipals = emptyForNull(clusterActionPrincipals);
        this.createPrincipals = emptyForNull(createPrincipals);
        this.describePrincipals = emptyForNull(describePrincipals);
        this.describeConfigsPrincipals = emptyForNull(describeConfigsPrincipals);
        this.idempotentWritePrincipals = emptyForNull(idempotentWritePrincipals);
    }

    public Set<String> principalsForOperation(AclOperation operation) {
        switch (operation) {
            case ALTER:
                return alterPrincipals;
            case ALTER_CONFIGS:
                return alterConfigsPrincipals;
            case CLUSTER_ACTION:
                return clusterActionPrincipals;
            case CREATE:
                return createPrincipals;
            case DESCRIBE:
                return describePrincipals;
            case DESCRIBE_CONFIGS:
                return describeConfigsPrincipals;
            case IDEMPOTENT_WRITE:
                return idempotentWritePrincipals;
            default:
                throw new UnsupportedOperationException("AclOperation " + operation + " is not a cluster-level operation");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClusterLevelAcls)) return false;
        ClusterLevelAcls that = (ClusterLevelAcls) o;
        return Objects.equals(alterPrincipals, that.alterPrincipals) &&
                Objects.equals(alterConfigsPrincipals, that.alterConfigsPrincipals) &&
                Objects.equals(clusterActionPrincipals, that.clusterActionPrincipals) &&
                Objects.equals(createPrincipals, that.createPrincipals) &&
                Objects.equals(describePrincipals, that.describePrincipals) &&
                Objects.equals(describeConfigsPrincipals, that.describeConfigsPrincipals) &&
                Objects.equals(idempotentWritePrincipals, that.idempotentWritePrincipals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alterPrincipals, alterConfigsPrincipals, clusterActionPrincipals, createPrincipals, describePrincipals, describeConfigsPrincipals, idempotentWritePrincipals);
    }

    @Override
    public String toString() {
        return "ClusterLevelAcls{" +
                "alterPrincipals=" + alterPrincipals +
                ", alterConfigsPrincipals=" + alterConfigsPrincipals +
                ", clusterActionPrincipals=" + clusterActionPrincipals +
                ", createPrincipals=" + createPrincipals +
                ", describePrincipals=" + describePrincipals +
                ", describeConfigsPrincipals=" + describeConfigsPrincipals +
                ", idempotentWritePrincipals=" + idempotentWritePrincipals +
                '}';
    }
}
