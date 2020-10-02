package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.protocol.types.Field;

import java.util.*;

public class ClusterState {
    @JsonProperty("aclsEntries")
    Set<ACLEntry> aclsEntries;
    @JsonProperty("roleBindings")
    Set<RbacRoleBinding> roleBindings;
    @JsonProperty("topicDescriptions")
    Map<String, TopicDescription> topicDescriptions;

    @JsonCreator
    public ClusterState(
            @JsonProperty("aclsEntries") Set<ACLEntry> aclsEntries,
            @JsonProperty("roleBindings") Set<RbacRoleBinding> roleBindings,
            @JsonProperty("topicDescriptions") Map<String, TopicDescription> topicDescriptions
    ) {
        this.aclsEntries = aclsEntries;
        this.roleBindings = roleBindings;
        this.topicDescriptions = topicDescriptions;
    }

    /**
     * returns a new Set containing the topic names in this clusterstate.
     * @return
     */
    public Set<String> topicNames() {
        return new HashSet<>(topicDescriptions.keySet());
    }

    /**
     * Filters the cluster state keep only those topics whose name starts with a given prefix.
     *
     * This is particular useful to delete/update topics for one project which is part of a larger
     * context: do a diff of the project description with a ClusterState filtered by the name-space
     * (prefix) of the project.
     * @param prefix
     * @return
     */
    public ClusterState filterByPrefix(String prefix) {
        /** Implementation note:
         *
         * maybe we should another constructor which automatically filters for the prefix.
         */
        Objects.requireNonNull(prefix);

        return new ClusterState(
                aclsEntries, //TODO: filter ACLs
                roleBindings, // TODO: filter rolebindings
                MapTools.filterKeys(topicDescriptions, s -> s.startsWith(prefix)));
    }

    public ClusterState merge(ClusterState other) {
        Set<RbacRoleBinding> mergedRoleBindings = new HashSet<>(this.roleBindings);
        mergedRoleBindings.addAll(other.roleBindings);

        Set<ACLEntry> mergedAclsEntries = new HashSet<>(this.aclsEntries);
        mergedAclsEntries.addAll(other.aclsEntries);

        Map<String, TopicDescription> mergedTopicDescriptions = new HashMap<>(this.topicDescriptions);
        mergedTopicDescriptions.putAll(other.topicDescriptions);
        return new ClusterState(
                mergedAclsEntries,
                mergedRoleBindings,
                mergedTopicDescriptions
        );
    }

    public static final ClusterState empty = new ClusterState(Collections.emptySet(), Collections.emptySet(), Collections.emptyMap());

    @Override
    public String toString() {
        return "ClusterState{" +
                "aclsEntries=" + aclsEntries +
                ", roleBindings=" + roleBindings +
                ", topicDescriptions=" + topicDescriptions +
                '}';
    }
}
