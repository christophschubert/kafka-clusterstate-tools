package net.christophschubert.kafka.clusterstate.policies;

import net.christophschubert.kafka.clusterstate.utils.Update;
import net.christophschubert.kafka.clusterstate.actions.Action;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfigUpdateDoNothingPolicy implements TopicConfigUpdatePolicy {
    @Override
    public List<Action> calcChanges(
            String topicName, Update<Map<String, String>> configChange) {
        return Collections.emptyList();
    }
}
