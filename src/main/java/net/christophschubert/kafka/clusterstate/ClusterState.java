package net.christophschubert.kafka.clusterstate;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClusterState {
    Set<ACLEntry> aclsEntries;
    Set<RbacRoleBinding> roleBindings;
    Map<String, TopicDescription> topicDescriptions;

    public ClusterState(Set<ACLEntry> aclsEntries, Set<RbacRoleBinding> roleBindings, Map<String, TopicDescription> topicDescriptions) {
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

    @Override
    public String toString() {
        return "ClusterState{" +
                "aclsEntries=" + aclsEntries +
                ", roleBindings=" + roleBindings +
                ", topicDescriptions=" + topicDescriptions +
                '}';
    }
}
