package net.christophschubert.kafka.clusterstate;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClusterStateDiff {
    final Set<ACLEntry> addedAclEntries;
    final Set<ACLEntry> deletedAclEntries;
    final Map<String, TopicDescription> addedTopics;
    final Set<String> deletedTopicNames;
    final Map<String, Update<TopicDescription>> updatedTopicConfigs;
    final Map<String, TopicDataModel> addedSchemaPaths;

     //TODO: add added/deleted RBAC bindings

    // typical use case: after is 'desired' state, before is 'current' state

    public ClusterStateDiff(ClusterState before, ClusterState after) {
        this.addedAclEntries = Sets.setMinus(after.aclsEntries, before.aclsEntries);
        this.deletedAclEntries = Sets.setMinus(before.aclsEntries, after.aclsEntries);
        //TODO: think about whether we have to prevent ACLs for internal topics (topics
        // created by streams app) to be removed
        // maybe this is no the case as we work with prefixed ACLs anyhow?

        final HashMap<String, TopicDescription> addedTopics = new HashMap<>(after.topicDescriptions);
        before.topicDescriptions.keySet().forEach(addedTopics::remove);
        this.addedTopics = addedTopics;

        final Set<String> deletedTopicNames = Sets.setMinus(
                before.topicDescriptions.keySet(), after.topicDescriptions.keySet());
        deletedTopicNames.removeIf(topicName ->
                after.managedTopicPrefixes.stream().anyMatch(topicName::startsWith)
        );
        this.deletedTopicNames = deletedTopicNames;

        final Set<String> intersectionTopicNames = Sets.intersection(
                before.topicDescriptions.keySet(),
                after.topicDescriptions.keySet());
        this.updatedTopicConfigs = new HashMap<>();
        intersectionTopicNames.forEach(topicName -> {
            final Map<String, String> beforeConfigs = before.topicDescriptions.get(topicName).configs();
            final Map<String, String> afterConfigs = after.topicDescriptions.get(topicName).configs();
            if (!beforeConfigs.equals(afterConfigs)) {
                updatedTopicConfigs.put(topicName, Update.of(before.topicDescriptions.get(topicName), after.topicDescriptions.get(topicName)));
            }
        });

        //currently, we just consider all schemas to be new
        this.addedSchemaPaths =
        MapTools.mapValues(after.topicDescriptions, TopicDescription::dataModel);
    }


}
