package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.mds.*;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ClusterStateTest {

    @Test
    public void mapPrincipalTest() {
        final var principalMap = Map.of("User:alias1", "User:final1", "User:alias2", "User:final2");

        final Scope scope = new Scope("testCluster", null);
        final var entryAlias1 = new ACLEntry(AclOperation.WRITE, "User:alias1", "*", AclPermissionType.ALLOW, "test", ResourceType.TOPIC, PatternType.LITERAL);
        final var entryAlias1Mapped = new ACLEntry(AclOperation.WRITE, "User:final1", "*", AclPermissionType.ALLOW, "test", ResourceType.TOPIC, PatternType.LITERAL);
        final var entryKeepMe = new ACLEntry(AclOperation.WRITE, "Group:keepme", "*", AclPermissionType.ALLOW, "test", ResourceType.TOPIC, PatternType.LITERAL);

        final var bindingAlias2 = new RbacBindingInScope(new RbacBinding("User:alias2", "ROLE", ResourcePattern.CLUSTERPATTERN), scope);
        final var bindingAlias2Mapped = new RbacBindingInScope(new RbacBinding("User:final2", "ROLE", ResourcePattern.CLUSTERPATTERN), scope);
        final var bindingKeepMe = new RbacBindingInScope(new RbacBinding("User:keepme", "AnotherRole", ResourcePattern.CLUSTERPATTERN), scope);
        final ClusterState original = new ClusterState(
                Set.of(entryAlias1, entryKeepMe),
                Set.of(bindingAlias2, bindingKeepMe),
                Collections.emptyMap(),
                Collections.emptySet()
        );

        final var mappedPrincipals = original.mapPrincipals(principalMap);
        assertEquals(Set.of(entryAlias1Mapped, entryKeepMe), mappedPrincipals.aclEntries);
        assertEquals(Set.of(bindingAlias2Mapped, bindingKeepMe), mappedPrincipals.roleBindings);
    }

    //TODO: add test for merge-method
    //TODO: add test for filterByPrefix
    //TODO: add test for filterToCluster
}