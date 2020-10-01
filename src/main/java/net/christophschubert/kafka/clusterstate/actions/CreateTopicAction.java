package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.TopicDescription;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CreateTopicAction implements Action {
    private final TopicDescription topic;

    public CreateTopicAction(TopicDescription topic) {
        this.topic = topic;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        //TODO: use configs when creating topic!
        final NewTopic newTopic = new NewTopic(topic.name(), Optional.empty(), Optional.empty());

        bundle.adminClient.createTopics(Collections.singleton(newTopic)).all().get();
        return false;
    }
}
