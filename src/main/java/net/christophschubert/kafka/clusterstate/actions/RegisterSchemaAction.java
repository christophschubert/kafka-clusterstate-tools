package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.TopicSchemaData;

import java.util.concurrent.ExecutionException;



public class RegisterSchemaAction implements Action {

    final String topicName;
    final TopicSchemaData topicSchemaData;

    public RegisterSchemaAction(String topicName, TopicSchemaData topicSchemaData) {
        this.topicName = topicName;
        this.topicSchemaData = topicSchemaData;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        return false;
    }
}
