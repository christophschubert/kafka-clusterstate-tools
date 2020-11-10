package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.mds.RbacBinding;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main model class to hold the static metadata state of a cluster.
 */
public class ClusterState {
    @JsonProperty("aclsEntries")
    Set<ACLEntry> aclsEntries;
    @JsonProperty("roleBindings")
    Set<RbacBindingInScope> roleBindings;
    @JsonProperty("topicDescriptions")
    Map<String, TopicDescription> topicDescriptions;

    @JsonProperty("managedTopicPrefixes")
    Set<String> managedTopicPrefixes;

    @JsonCreator
    public ClusterState(
            @JsonProperty("aclsEntries") Set<ACLEntry> aclsEntries,
            @JsonProperty("roleBindings") Set<RbacBindingInScope> roleBindings,
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
                Sets.filter(aclsEntries, aclEntry -> aclEntry.resourceName().startsWith(prefix)),
                roleBindings, // TODO: filter rolebindings
                MapTools.filterKeys(topicDescriptions, s -> s.startsWith(prefix)),
                managedTopicPrefixes // TODO: should this be filtered?
        );
    }

    /**
     * Change principals in the according to the mapping provided.
     *
     * @param principalMap mapping of the principal-names
     * @return a new ClusterState with rewritten principals
     */
    public ClusterState mapPrincipals(Map<String, String> principalMap) {
         return new ClusterState(
                 this.aclsEntries.stream().map(aclEntry -> {
                     if (principalMap.containsKey(aclEntry.principal)) {
                         return new ACLEntry(
                                 aclEntry.operation,
                                 principalMap.get(aclEntry.principal),
                                 aclEntry.host,
                                 aclEntry.permissionType,
                                 aclEntry.resourceName,
                                 aclEntry.resourceType,
                                 aclEntry.patternType
                         );
                     }
                     return aclEntry;
                 }).collect(Collectors.toSet()),
                 this.roleBindings.stream().map(bindingInScope -> {
                     final String originalPrincipal = bindingInScope.binding.principal;
                     if (principalMap.containsKey(originalPrincipal)) {
                         return new RbacBindingInScope(
                                 new RbacBinding(
                                         principalMap.get(originalPrincipal),
                                         bindingInScope.binding.roleName,
                                         bindingInScope.binding.resourcePattern
                                 ),
                                 bindingInScope.scope);
                     }
                     return bindingInScope;
                 }).collect(Collectors.toSet()),
                 this.topicDescriptions,
                 this.managedTopicPrefixes
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
