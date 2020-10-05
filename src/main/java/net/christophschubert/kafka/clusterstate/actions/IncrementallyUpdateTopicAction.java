package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class IncrementallyUpdateTopicAction implements Action {

    final String topicName;
    final Map<String, String> topicConfig;

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
        final var adminClient = bundle.adminClient;

        try {
            adminClient.incrementalAlterConfigs(buildParameters()).all().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    Map<ConfigResource, Collection<AlterConfigOp>> buildParameters() {
        Collection<AlterConfigOp> ops = new ArrayList<>();

        topicConfig.forEach(
                (k, v) -> {
                    ConfigEntry entry = new ConfigEntry(k, v);
                    AlterConfigOp op = new AlterConfigOp(entry, AlterConfigOp.OpType.SET);
                    ops.add(op);
                });
        return Collections.singletonMap(new ConfigResource(ConfigResource.Type.TOPIC, topicName), ops);
    }
}
