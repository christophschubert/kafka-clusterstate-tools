package net.christophschubert.kafka.clusterstate;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClusterStateDiff {
    final Set<ACLEntry> addedAclEntries;
    final Set<ACLEntry> deletedAclEntries;
    final Map<String, TopicDescription> addedTopics;
    final Set<String> deletedTopicNames;
    final Map<String, Pair<TopicDescription, TopicDescription>> updatedTopicConfigs;
     //TODO: add added/deleted RBAC bindings

    public ClusterStateDiff(ClusterState before, ClusterState after) {
        final HashSet<ACLEntry> addedEntries = new HashSet<>(after.aclsEntries);
        addedEntries.removeAll(before.aclsEntries);
        this.addedAclEntries = addedEntries;

        final HashSet<ACLEntry> deletedAclEntries = new HashSet<>(before.aclsEntries);
        deletedAclEntries.removeAll(after.aclsEntries);
        this.deletedAclEntries = deletedAclEntries;

        final HashMap<String, TopicDescription> addedTopics = new HashMap<>(after.topicDescriptions);
        before.topicDescriptions.keySet().forEach(topicName -> addedTopics.remove(topicName));
        this.addedTopics = addedTopics;

        final HashSet<String> deletedTopicNames = new HashSet<>(before.topicDescriptions.keySet());
        deletedTopicNames.removeAll(after.topicDescriptions.keySet());
        this.deletedTopicNames = deletedTopicNames;

        final HashSet<String> intersectionTopicNames = new HashSet<>(before.topicDescriptions.keySet());
        intersectionTopicNames.retainAll(after.topicDescriptions.keySet());
        this.updatedTopicConfigs = new HashMap<>();
        intersectionTopicNames.forEach(topicName -> {
            final Map<String, String> beforeConfigs = before.topicDescriptions.get(topicName).configs();
            final Map<String, String> afterConfigs = after.topicDescriptions.get(topicName).configs();
            if (!beforeConfigs.equals(afterConfigs)) {
                updatedTopicConfigs.put(topicName, Pair.of(before.topicDescriptions.get(topicName), after.topicDescriptions.get(topicName)));
            }
        });
    }


}
