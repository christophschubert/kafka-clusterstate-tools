package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class ClusterState {
    @JsonProperty("aclsEntries")
    Set<ACLEntry> aclsEntries;
    @JsonProperty("roleBindings")
    Set<RbacRoleBinding> roleBindings;
    @JsonProperty("topicDescriptions")
    Map<String, TopicDescription> topicDescriptions;

    @JsonProperty("managedTopicPrefixes")
    Set<String> managedTopicPrefixes;

    @JsonCreator
    public ClusterState(
            @JsonProperty("aclsEntries") Set<ACLEntry> aclsEntries,
            @JsonProperty("roleBindings") Set<RbacRoleBinding> roleBindings,
            @JsonProperty("topicDescriptions") Map<String, TopicDescription> topicDescriptions,
            @JsonProperty("managedTopicPrefixes") Set<String> managedTopicPrefixes
    ) {
        this.aclsEntries = aclsEntries;
        this.roleBindings = roleBindings;
        this.topicDescriptions = topicDescriptions;
        this.managedTopicPrefixes = managedTopicPrefixes;
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
                MapTools.filterKeys(topicDescriptions, s -> s.startsWith(prefix)),
                managedTopicPrefixes // TODO: should this be filtered?
        );
    }

    public ClusterState merge(ClusterState other) {
        final var mergedRoleBindings = Sets.union(this.roleBindings, other.roleBindings);
        final var mergedAclEntries = Sets.union(this.aclsEntries, other.aclsEntries);
        final var mergedTopicDescriptions = new HashMap<>(this.topicDescriptions);
        mergedTopicDescriptions.putAll(other.topicDescriptions);

        return new ClusterState(
                mergedAclEntries,
                mergedRoleBindings,
                mergedTopicDescriptions,
                Sets.union(this.managedTopicPrefixes, other.managedTopicPrefixes)
        );
    }



    public static final ClusterState empty = new ClusterState(
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptyMap(),
            Collections.emptySet()
    );

    @Override
    public String toString() {
        return "ClusterState{" +
                "aclsEntries=" + aclsEntries +
                ", roleBindings=" + roleBindings +
                ", topicDescriptions=" + topicDescriptions +
                ", managedTopicPrefixes=" + managedTopicPrefixes +
                '}';
    }
}
