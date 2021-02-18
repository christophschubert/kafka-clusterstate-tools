package net.christophschubert.kafka.clusterstate.formats.cluster.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.AclEntries;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelAcls;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelPrivileges;
import net.christophschubert.kafka.clusterstate.formats.cluster.Namespace;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.HashSet;
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

    Set<ACLEntry> clusterWideAcls(ClusterLevelAcls aclDefinitions) {
        return clusterLevelOperations.stream().flatMap(
                op -> aclDefinitions.principalsForOperation(op).stream()
                        .map(principal -> makeEntry(op, principal))
        ).collect(Collectors.toSet());
    }

    Set<ACLEntry> entriesForNameSpace(Namespace namespace) {
        final String name = namespace.name;
        final Set<ACLEntry> entries = new HashSet<>();

        if (Boolean.TRUE.equals(namespace.useWildCardGroup)) {
            namespace.consumerPrincipals.forEach(principal ->
                entries.addAll(AclEntries.topicPrefixConsumerWildcard(principal, name))
            );
        } else {
            namespace.consumerPrincipals.forEach(principal ->
                    entries.addAll(AclEntries.topicPrefixConsumerPrefix(principal, name, name))
            );
        }
        namespace.producerPrincipals.forEach(principal -> entries.addAll(AclEntries.topicPrefixProducer(principal, name)));

        final Set<AclOperation> managementOperations = Set.of(
                AclOperation.CREATE,
                AclOperation.DELETE,
                AclOperation.DESCRIBE,
                AclOperation.DESCRIBE_CONFIGS,
                AclOperation.ALTER,
                AclOperation.ALTER_CONFIGS
        );

        namespace.managerPrincipals.forEach(principal ->
                managementOperations.forEach(op ->
                    entries.add(AclEntries.allowAnyHostPrefix(op, principal, name, ResourceType.TOPIC))
                )
        );

        return entries;
    }

    public ClusterState compile (ClusterLevelPrivileges privileges) {
        final Set<ACLEntry> aclEntries = new HashSet<>();

        aclEntries.addAll(clusterWideAcls(privileges.acls));

        privileges.namespaces.forEach(namespace -> aclEntries.addAll(entriesForNameSpace(namespace)));

        final Set<String> managedPrefixes = privileges.namespaces.stream().map(ns -> ns.name).collect(Collectors.toSet());

        return ClusterState.builder()
                .withAclEntries(aclEntries)
                .withManagedTopicPrefixes(managedPrefixes)
                .build();
    }
}
