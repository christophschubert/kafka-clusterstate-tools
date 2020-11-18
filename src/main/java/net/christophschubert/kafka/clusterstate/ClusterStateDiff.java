package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;
import net.christophschubert.kafka.clusterstate.utils.MapTools;
import net.christophschubert.kafka.clusterstate.utils.Sets;
import net.christophschubert.kafka.clusterstate.utils.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClusterStateDiff {
    final Set<ACLEntry> addedAclEntries;
    final Set<ACLEntry> deletedAclEntries;

    final Set<RbacBindingInScope> addedRbacBindings;
    final Set<RbacBindingInScope> deletedRbacBindings;

    final Map<String, TopicDescription> addedTopics;
    final Set<String> deletedTopicNames;
    final Map<String, Update<TopicDescription>> updatedTopicConfigs;
    final Map<String, TopicDataModel> addedSchemaPaths;

    // typical use case: after is 'desired' state, before is 'current' state

    public ClusterStateDiff(ClusterState before, ClusterState after) {
        this.addedAclEntries = Sets.setMinus(after.aclEntries, before.aclEntries);
        this.deletedAclEntries = Sets.setMinus(before.aclEntries, after.aclEntries);

        this.addedRbacBindings = Sets.setMinus(after.roleBindings, before.roleBindings);
        this.deletedRbacBindings = Sets.setMinus(before.roleBindings, after.roleBindings);

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
        MapTools.mapValuesDropNull(after.topicDescriptions, TopicDescription::dataModel);
    }


}
