package net.christophschubert.kafka.clusterstate.formats.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.table.TableCellEditor;
import java.util.Set;
import static net.christophschubert.kafka.clusterstate.formats.Helpers.emptyForNull;

public class KafkaCluster {
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
    public KafkaCluster(
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

}
