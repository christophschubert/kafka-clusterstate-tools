package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClientBundle;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class CreateAclAction implements Action {
    private final ACLEntry entry;

    public CreateAclAction(ACLEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        bundle.adminClient.createAcls(Collections.singleton(entry.toAclBinding())).all().get();
        return false;
    }

    @Override
    public String toString() {
        return "CreateAclAction{" +
                "entry=" + entry +
                '}';
    }
}
