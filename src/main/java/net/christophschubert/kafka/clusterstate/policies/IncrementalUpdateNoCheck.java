package net.christophschubert.kafka.clusterstate.policies;

import net.christophschubert.kafka.clusterstate.utils.Update;
import net.christophschubert.kafka.clusterstate.actions.Action;
import net.christophschubert.kafka.clusterstate.actions.IncrementallyUpdateTopicAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncrementalUpdateNoCheck implements TopicConfigUpdatePolicy {
    /**
     * Incrementally applies the new settings from 'desiredConfig'.
     *
     * <p> Settings which are part of currentConfig but not of desiredConfig will not be changed.
     *
     * @param topicName
     * @param configChange
     * @return
     **/

    @Override
    public List<Action> calcChanges(String topicName, Update<Map<String, String>> configChange) {
        final Map<String, String> updatedConfigs = new HashMap<>();

        configChange.after.forEach((dKey, dValue) -> {
                    final var current = configChange.before;
                    if (!current.containsKey(dKey) || !current.get(dKey).equals(dValue)) {
                        updatedConfigs.put(dKey, dValue);
                    }
                });
        if (updatedConfigs.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new IncrementallyUpdateTopicAction(topicName, updatedConfigs));
        }
    }
}
