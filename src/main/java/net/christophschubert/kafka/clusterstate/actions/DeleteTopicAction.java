package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class DeleteTopicAction implements Action {
    private final String topicName;

    public DeleteTopicAction(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {

        bundle.adminClient.deleteTopics(Collections.singleton(topicName));
        return false;
    }

    @Override
    public String toString() {
        return "DeleteTopicAction{" +
                "topicName='" + topicName + '\'' +
                '}';
    }
}
