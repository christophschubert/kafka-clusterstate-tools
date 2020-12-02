package net.christophschubert.kafka.clusterstate.actions;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import org.apache.kafka.clients.admin.MockAdminClient;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class DeleteTopicActionTest {

    void addTopics(MockAdminClient adminClient, Set<String> topicNames) {
        final Node node = adminClient.broker(0);
        topicNames.forEach(topicName -> adminClient.addTopic(false, topicName,
                List.of(new TopicPartitionInfo(0,node , List.of(node), List.of(node))),
                Collections.EMPTY_MAP));
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        final MockAdminClient adminClient = MockAdminClient.create().numBrokers(3).controller(0).build();

        final String topicToDelete = "deleteMe";
        final String remainingTopic = "deleteMeNot";

        addTopics(adminClient, Set.of(topicToDelete, remainingTopic));

        ClientBundle bundle = new ClientBundle(adminClient, null, new MockSchemaRegistryClient());

        assertEquals(2, adminClient.listTopics().names().get().size());

        DeleteTopicAction action = new DeleteTopicAction(topicToDelete);
        action.runRaw(bundle);

        assertEquals(Set.of(remainingTopic), adminClient.listTopics().names().get());
    }


}