package net.christophschubert.kafka.clusterstate.formats.cluster.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelPrivileges;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.Set;
import java.util.stream.Collectors;

public class DefaultCompiler {
    public static final Set<AclOperation> clusterLevelOperations =
            Set.of(AclOperation.ALTER, AclOperation.ALTER_CONFIGS, AclOperation.CLUSTER_ACTION, AclOperation.CREATE,
                    AclOperation.DESCRIBE, AclOperation.DESCRIBE_CONFIGS, AclOperation.IDEMPOTENT_WRITE);

    private ACLEntry makeEntry(AclOperation operation, String principal) {
        return new ACLEntry(operation, principal,
                "*", AclPermissionType.ALLOW, "", ResourceType.CLUSTER, PatternType.LITERAL);
    }

    public ClusterState compile (ClusterLevelPrivileges privileges) {
        final Set<ACLEntry> entries = clusterLevelOperations.stream().flatMap(
                op -> privileges.acls.principalsForOperation(op).stream()
                        .map(principal -> makeEntry(op, principal))
        ).collect(Collectors.toSet());

        return ClusterState.builder().withAclEntries(entries).build();
    }
}
