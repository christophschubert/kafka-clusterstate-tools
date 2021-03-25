package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DeleteMultipleAclsAction implements Action {

    private final Collection<ACLEntry> entries;

    public DeleteMultipleAclsAction(Collection<ACLEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {
        final List<AclBindingFilter> filters = entries.stream()
                .map(ACLEntry::toAclBinding)
                .map(AclBinding::toFilter)
                .collect(Collectors.toList());

        bundle.adminClient.deleteAcls(filters).all().get();
        return false;
    }

    @Override
    public String toString() {
        return "DeleteMultipleAclsAction{" + entries + '}';
    }
}
