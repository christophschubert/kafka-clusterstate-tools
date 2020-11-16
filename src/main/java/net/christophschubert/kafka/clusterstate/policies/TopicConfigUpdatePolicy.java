package net.christophschubert.kafka.clusterstate.policies;

import net.christophschubert.kafka.clusterstate.utils.Update;
import net.christophschubert.kafka.clusterstate.actions.Action;

import java.util.List;
import java.util.Map;

/**
 * Strategy to calculate the necessary actions when a topic-configuration changes.
 */
public interface TopicConfigUpdatePolicy {
    List<Action> calcChanges(String topicName, Update<Map<String, String>> configChange);
}
