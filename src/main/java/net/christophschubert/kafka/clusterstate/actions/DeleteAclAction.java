package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClientBundle;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class DeleteAclAction implements Action {
    private final ACLEntry entry;

    public DeleteAclAction(ACLEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        bundle.adminClient.deleteAcls(Collections.singleton(entry.toAclBinding().toFilter())).all().get();
        return false;
    }

    @Override
    public String toString() {
        return "DeleteAclAction{" +
                "entry=" + entry +
                '}';
    }
}
