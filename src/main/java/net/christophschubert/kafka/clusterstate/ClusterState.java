package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.christophschubert.kafka.clusterstate.mds.RbacBinding;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;
import net.christophschubert.kafka.clusterstate.utils.MapTools;
import net.christophschubert.kafka.clusterstate.utils.Sets;
import org.apache.kafka.common.resource.ResourceType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main model class to hold the static metadata state of a cluster.
 */
public class ClusterState {
    @JsonProperty("aclEntries")
    public final Set<ACLEntry> aclEntries;
    @JsonProperty("roleBindings")
    public final Set<RbacBindingInScope> roleBindings;
    @JsonProperty("topicDescriptions")
    public final Map<String, TopicDescription> topicDescriptions;

    @JsonProperty("managedTopicPrefixes")
    public final Set<String> managedTopicPrefixes;

    @JsonCreator
    public ClusterState(
            @JsonProperty("aclEntries") Set<ACLEntry> aclEntries,
            @JsonProperty("roleBindings") Set<RbacBindingInScope> roleBindings,
            @JsonProperty("topicDescriptions") Map<String, TopicDescription> topicDescriptions,
            @JsonProperty("managedTopicPrefixes") Set<String> managedTopicPrefixes
    ) {
        this.aclEntries = aclEntries;
        this.roleBindings = roleBindings;
        this.topicDescriptions = topicDescriptions;
        this.managedTopicPrefixes = managedTopicPrefixes;
    }

    /**
     * returns a new Set containing the topic names in this clusterstate.
     * @return names of all topics in the topicDescriptions.
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
     * @param prefix resource-name prefix
     * @return a new ClusterState which contains only resources with the given prefix.
     */
    public ClusterState filterByPrefix(String prefix) {
        /* Implementation note:
         * maybe we should another constructor which automatically filters for the prefix.
         */
        Objects.requireNonNull(prefix);

        return new ClusterState(
                Sets.filter(aclEntries, aclEntry -> aclEntry.resourceName().startsWith(prefix)),
                Sets.filter(roleBindings, rb -> rb.binding.resourcePattern.name.startsWith(prefix)),
                MapTools.filterKeys(topicDescriptions, s -> s.startsWith(prefix)),
                managedTopicPrefixes // TODO: should this be filtered?
        );
    }

    /**
     * Returns a copy of this cluster state containing only the cluster-level ACLs
     * and RBAC rolebindings.
     *
     * @return a copy of this ClusterState containing only cluster-level role and ACL bindings.
     */
    public ClusterState filterClusterLevel() {
        return new Builder(this)
                .withAclEntries(Sets.filter(this.aclEntries, entry -> entry.resourceType == ResourceType.CLUSTER))
                .withRoleBindingsEntries(Sets.filter(this.roleBindings, RbacBindingInScope::isClusterLevel))
                .withTopicDescriptions(Collections.emptyMap())
                .build();
    }

    /**
     * Change principals in the according to the mapping provided.
     *
     * @param principalMap mapping of the principal-names
     * @return a new ClusterState with rewritten principals
     */
    public ClusterState mapPrincipals(Map<String, String> principalMap) {
         return new ClusterState(
                 this.aclEntries.stream().map(aclEntry -> {
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
        final var mergedAclEntries = Sets.union(this.aclEntries, other.aclEntries);
        final var mergedTopicDescriptions = new HashMap<>(this.topicDescriptions);
        mergedTopicDescriptions.putAll(other.topicDescriptions);

        return new ClusterState(
                mergedAclEntries,
                mergedRoleBindings,
                mergedTopicDescriptions,
                Sets.union(this.managedTopicPrefixes, other.managedTopicPrefixes)
        );
    }


    public static final ClusterState empty = new ClusterState.Builder().build();


    @Override
    public String toString() {
        return "ClusterState{" +
                "aclEntries=" + aclEntries +
                ", roleBindings=" + roleBindings +
                ", topicDescriptions=" + topicDescriptions +
                ", managedTopicPrefixes=" + managedTopicPrefixes +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Set<ACLEntry> aclsEntries;
        private Set<RbacBindingInScope> roleBindings;
        private Map<String, TopicDescription> topicDescriptions;
        private Set<String> managedTopicPrefixes;

        public Builder() {
            aclsEntries = Collections.emptySet();
            roleBindings = Collections.emptySet();
            topicDescriptions = Collections.emptyMap();
            managedTopicPrefixes = Collections.emptySet();
        }

        public Builder(ClusterState state) {
            this.aclsEntries = state.aclEntries;
            this.roleBindings = state.roleBindings;
            this.topicDescriptions = state.topicDescriptions;
            this.managedTopicPrefixes = state.managedTopicPrefixes;
        }

        public Builder withAclEntries(Set<ACLEntry> entries) {
            this.aclsEntries = entries;
            return this;
        }

        public Builder withRoleBindingsEntries(Set<RbacBindingInScope> roleBindings) {
            this.roleBindings = roleBindings;
            return this;
        }

        public Builder withTopicDescriptions(Map<String, TopicDescription> topicDescriptions) {
            this.topicDescriptions = topicDescriptions;
            return this;
        }

        public Builder withManagedTopicPrefixes(Set<String> managedTopicPrefixes) {
            this.managedTopicPrefixes = managedTopicPrefixes;
            return this;
        }

        public ClusterState build() {
            return new ClusterState(aclsEntries, roleBindings, topicDescriptions, managedTopicPrefixes);
        }
    }
}
