package net.christophschubert.kafka.clusterstate.mds;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MdsTools {

    /**
     * Extract all RBAC rolebindings for a Kafka cluster.
     *
     * @param client
     * @param scope
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Set<RbacBindingInScope> extractAllRolebindings(MdsClient client, Scope scope) throws IOException, InterruptedException {
        //strategy to extract all RBAC role bindings:
        // 1. list all rolenames
        // 2. for a fixed scope (just one Kafka cluster in the MVP), get all principals who have the given role
        // 3. for each principal, get all rolebindings



        final var roleNames = client.roleNames();

        final var allPrincipals = roleNames.stream().flatMap(roleName -> {
            try { //TODO: fix this mess!
                return client.principalsForRole(roleName, scope).stream();
            } catch (Exception e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(Collectors.toSet());

        final Set<RbacBindingInScope> rbacBindings = new HashSet<>();
        allPrincipals.forEach(principal -> {
            try {
                final var bfp = client.bindingsForPrincipal(principal, scope);
                bfp.forEach((bindingPrincipal, rbs) -> {
                    rbs.forEach((roleName, patters) -> {
                        if (patters.isEmpty()) {
                            rbacBindings.add(new RbacBindingInScope(
                                    new RbacBinding(bindingPrincipal, roleName, ResourcePattern.CLUSTERPATTERN), scope));
                        }
                        for (ResourcePattern pattern : patters) {
                            rbacBindings.add(new RbacBindingInScope(new RbacBinding(bindingPrincipal, roleName, pattern), scope));
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return rbacBindings;
    }

}
