package net.christophschubert.kafka.clusterstate.formats.cluster.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelPrivileges;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class DefaultCompiler {
    private ACLEntry makeEntry(String principal, AclOperation operation) {
        return new ACLEntry(operation, principal,
                "*", AclPermissionType.ALLOW, "", ResourceType.CLUSTER, PatternType.LITERAL);
    }

    public ClusterState compile (ClusterLevelPrivileges privileges) {
        final var acls = privileges.acls;
        final Set<ACLEntry> entries = new HashSet<>();

        final BiConsumer<Set<String>, AclOperation> adder = (principals, operation)  ->
                principals.forEach(p -> entries.add(makeEntry(p, operation)));

        adder.accept(acls.alterConfigsPrincipals, AclOperation.ALTER_CONFIGS);
        adder.accept(acls.alterPrincipals, AclOperation.ALTER);
        adder.accept(acls.clusterActionPrincipals, AclOperation.CLUSTER_ACTION);
        adder.accept(acls.createPrincipals, AclOperation.CREATE);
        adder.accept(acls.deletePrincipals, AclOperation.DELETE);
        adder.accept(acls.describePrincipals, AclOperation.DESCRIBE);
        adder.accept(acls.describeConfigsPrincipals, AclOperation.DESCRIBE_CONFIGS);
        adder.accept(acls.idempotentWritePrincipals, AclOperation.IDEMPOTENT_WRITE);

        return ClusterState.builder().withAclEntries(entries).build();
    }
}
