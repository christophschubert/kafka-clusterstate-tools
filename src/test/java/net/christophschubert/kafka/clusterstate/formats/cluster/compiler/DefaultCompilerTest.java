package net.christophschubert.kafka.clusterstate.formats.cluster.compiler;

import net.christophschubert.kafka.clusterstate.AclEntries;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelAcls;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelPrivileges;
import net.christophschubert.kafka.clusterstate.formats.cluster.Namespace;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultCompilerTest {
    @Test
    public void compilerTestClusterLevel() {
        final ClusterLevelPrivileges privileges = new ClusterLevelPrivileges(
                new ClusterLevelAcls(
                        Set.of("alter:1", "alter:2"),
                        Set.of("alterConfig:1"),
                        Set.of("clusterAction:1"),
                        Set.of("create:1"),
                        Set.of("describe:1", "describe:2"),
                        Set.of("describeConfig:1"),
                        Set.of("idempotentWrite:1")
                )
        );

        DefaultCompiler compiler = new DefaultCompiler();

        final var aclEntries = compiler.compile(privileges).aclEntries;

        assertEquals(9, aclEntries.size());
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.ALTER, "alter:1", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.ALTER, "alter:2", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.ALTER_CONFIGS, "alterConfig:1", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.CLUSTER_ACTION, "clusterAction:1", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.CREATE, "create:1", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.DESCRIBE, "describe:1", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.DESCRIBE, "describe:2", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.DESCRIBE_CONFIGS, "describeConfig:1", "", ResourceType.CLUSTER)));
        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.IDEMPOTENT_WRITE, "idempotentWrite:1", "", ResourceType.CLUSTER)));
    }

    @Test
    public void rightNumberOfClusterLevelAcls() {
        assertEquals(7, DefaultCompiler.clusterLevelOperations.size());
    }

    @Test
    public void compilerNamespaceWildcardGroup() {
        final ClusterLevelPrivileges privileges = new ClusterLevelPrivileges(
                new ClusterLevelAcls(
                        Set.of("alter:1", "alter:2"),
                        Set.of("alterConfig:1"),
                        Set.of("clusterAction:1"),
                        Set.of("create:1"),
                        Set.of("describe:1", "describe:2"),
                        Set.of("describeConfig:1"),
                        Set.of("idempotentWrite:1")
                ),
                Set.of(new Namespace("namespace", Set.of("User:manager1"), Set.of("User:consumer1", "User:consumer2"), Set.of("User:producer-1"), true))
        );

        final DefaultCompiler compiler = new DefaultCompiler();


        compiler.compile(privileges).aclEntries.forEach(System.out::println);

    }
}