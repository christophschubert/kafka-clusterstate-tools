package net.christophschubert.kafka.clusterstate.formats.cluster.compiler;

import net.christophschubert.kafka.clusterstate.AclEntries;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelAcls;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelPrivileges;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultCompilerTest {
    @Test
    public void compilerTest() {
        final ClusterLevelPrivileges privileges = new ClusterLevelPrivileges(
                new ClusterLevelAcls(Set.of("alterPrin:1", "alterPrin:2"),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Set.of("idempotent:1")
                        )
        );

        DefaultCompiler compiler = new DefaultCompiler();

        final var aclEntries = compiler.compile(privileges).aclEntries;

        assertTrue(aclEntries.contains(AclEntries.allowAnyHostLiteral(AclOperation.IDEMPOTENT_WRITE, "idempotent:1", "", ResourceType.CLUSTER)));
        assertEquals(3, aclEntries.size());
    }
}