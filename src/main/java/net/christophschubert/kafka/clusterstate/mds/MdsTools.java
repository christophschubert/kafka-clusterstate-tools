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
     * @param kafkaClusterId
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Set<RbacBinding> extractAllRolebindings(MdsClient client, String kafkaClusterId) throws IOException, InterruptedException {
        //strategy to extract all RBAC role bindings:
        // 1. list all rolenames
        // 2. for a fixed scope (just one Kafka cluster in the MVP), get all principals who have the given role
        // 3. for each principal, get all rolebindings

        final Scope kafkaScope = Scope.forClusterId(kafkaClusterId);

        final var roleNames = client.roleNames();
        System.out.println(roleNames);
        final var allPrincipals = roleNames.stream().flatMap(roleName -> {
            try { //TODO: fix this mess!
                return client.principalsForRole(roleName, kafkaScope).stream();
            } catch (Exception e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(Collectors.toSet());
        System.out.println(allPrincipals);

        final Set<RbacBinding> rbacBindings = new HashSet<>();
        allPrincipals.forEach(principal -> {
            try {
                final var bfp = client.bindingsForPrincipal(principal, kafkaScope);
                System.out.println("    " + bfp);
                bfp.forEach((bindingPrincipal, rbs) -> {
                    rbs.forEach((roleName, patters) -> {
                        if (patters.isEmpty()) {
                            rbacBindings.add(new RbacBinding(bindingPrincipal, roleName, ResourcePattern.CLUSTERPATTERN));
                        }
                        for (ResourcePattern pattern : patters) {
                            rbacBindings.add(new RbacBinding(bindingPrincipal, roleName, pattern));
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
