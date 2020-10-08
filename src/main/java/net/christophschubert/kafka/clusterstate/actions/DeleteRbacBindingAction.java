package net.christophschubert.kafka.clusterstate.actions;

import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.mds.MdsClient;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;

import java.util.Collections;
import java.util.concurrent.ExecutionException;


public class DeleteRbacBindingAction implements Action {

    private final RbacBindingInScope bindingInScope;


    public DeleteRbacBindingAction(RbacBindingInScope bindingInScope) {
        this.bindingInScope = bindingInScope;
    }

    @Override
    public boolean runRaw(ClientBundle bundle) throws InterruptedException, ExecutionException {

        final MdsClient client = bundle.mdsClient;
        final String principal = bindingInScope.binding.principal;
        final String roleName = bindingInScope.binding.roleName;
        try {
            if (bindingInScope.binding.resourcePattern.isSystemPattern()) {
                client.unbindClusterRole(principal, roleName, bindingInScope.scope);
            } else {
                client.removeBinding(principal, roleName, bindingInScope.scope, Collections.singletonList(bindingInScope.binding.resourcePattern));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "DeleteRbacBindingAction{" +
                "binding=" + bindingInScope +
                '}';
    }
}
