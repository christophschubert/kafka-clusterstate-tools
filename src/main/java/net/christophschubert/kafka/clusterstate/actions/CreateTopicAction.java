package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.TopicDescription;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CreateTopicAction implements Action {

    final static String REPLICATION_FACTOR_KEY = "replication.factor";
    final static String NUM_PARTITIONS_KEY = "num.partitions";

    private final TopicDescription topic;

    public CreateTopicAction(TopicDescription topic) {
        this.topic = topic;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        bundle.adminClient.createTopics(buildParameters()).all().get();
        return false;
    }

    Collection<NewTopic> buildParameters() {
        final var configs = topic.configs();
        final var replicationFactor = Optional.ofNullable(configs.get(REPLICATION_FACTOR_KEY))
                .map(Short::parseShort);
        final var numPartitions = Optional.ofNullable(configs.get(NUM_PARTITIONS_KEY)).map(Integer::parseInt);
        configs.remove(NUM_PARTITIONS_KEY);

        //TODO: add option for replica placement
        final NewTopic newTopic = new NewTopic(topic.name(), numPartitions, replicationFactor);
        newTopic.configs(configs);
        //TODO: check if explicitly setting numPartitions and replication factor in constructor is
        // necessary when we use configs.
        return Collections.singleton(newTopic);
    }

    @Override
    public String toString() {
        return "CreateTopicAction{" +
                "topic=" + topic +
                '}';
    }
}
