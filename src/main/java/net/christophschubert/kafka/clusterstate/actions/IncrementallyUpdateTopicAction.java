package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class IncrementallyUpdateTopicAction implements Action {

    private final String topicName;
    private final Map<String, String> topicConfig;

    public IncrementallyUpdateTopicAction(String topicName, Map<String, String> topicConfig) {
        this.topicName = topicName;
        this.topicConfig = topicConfig;
    }

    @Override
    public String toString() {
        return "Action: update settings for topic  " + topicName + " " + topicConfig;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        bundle.adminClient.incrementalAlterConfigs(buildParameters()).all().get();
        return false;
    }

    Map<ConfigResource, Collection<AlterConfigOp>> buildParameters() {
        final var configOps = topicConfig.entrySet().stream()
                .map(e -> new ConfigEntry(e.getKey(), e.getValue()))
                .map(ce -> new AlterConfigOp(ce, AlterConfigOp.OpType.SET))
                .collect(Collectors.toSet());
        final var configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        return Collections.singletonMap(configResource, configOps);
    }
}
