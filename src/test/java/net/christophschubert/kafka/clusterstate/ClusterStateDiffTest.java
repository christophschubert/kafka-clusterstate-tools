package net.christophschubert.kafka.clusterstate;

import net.christophschubert.kafka.clusterstate.mds.RbacBinding;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;
import net.christophschubert.kafka.clusterstate.mds.ResourcePattern;
import net.christophschubert.kafka.clusterstate.mds.Scope;
import net.christophschubert.kafka.clusterstate.utils.Update;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ClusterStateDiffTest {
    @Test
    public void aclDiffTest() {
        final ACLEntry writeA = AclEntries.allowAnyHostLiteral(AclOperation.WRITE, "User:a", "test", ResourceType.TOPIC);
        final ACLEntry writeB = AclEntries.allowAnyHostLiteral(AclOperation.WRITE, "User:b", "test", ResourceType.TOPIC);
        final ACLEntry writeC = AclEntries.allowAnyHostLiteral(AclOperation.WRITE, "User:c", "test", ResourceType.TOPIC);

        final ClusterState before = ClusterState.builder().withAclEntries(Set.of(writeA, writeB)).build();
        final ClusterState after = ClusterState.builder().withAclEntries(Set.of(writeA, writeC)).build();

        final var stateDiff = new ClusterStateDiff(before, after);

        assertEquals(Set.of(writeC), stateDiff.addedAclEntries);
        assertEquals(Set.of(writeB), stateDiff.deletedAclEntries);
        assertEquals(Collections.emptySet(), stateDiff.addedRbacBindings);
        assertEquals(Collections.emptySet(), stateDiff.deletedRbacBindings);
        assertEquals(Collections.emptySet(), stateDiff.deletedTopicNames);
        assertEquals(Collections.emptyMap(), stateDiff.addedTopics);
        assertEquals(Collections.emptyMap(), stateDiff.updatedTopicConfigs);
    }

    @Test
    public void rbacDiffTest() {
        final RbacBindingInScope bindingA = new RbacBindingInScope(new RbacBinding("User:a", "ClusterAdmin", ResourcePattern.CLUSTERPATTERN), Scope.forClusterName("test"));
        final RbacBindingInScope bindingB = new RbacBindingInScope(new RbacBinding("User:b", "ClusterAdmin", ResourcePattern.CLUSTERPATTERN), Scope.forClusterName("test"));
        final RbacBindingInScope bindingC = new RbacBindingInScope(new RbacBinding("User:c", "ClusterAdmin", ResourcePattern.CLUSTERPATTERN), Scope.forClusterName("test"));

        final ClusterState before = ClusterState.builder().withRoleBindingsEntries(Set.of(bindingA, bindingB)).build();
        final ClusterState after = ClusterState.builder().withRoleBindingsEntries(Set.of(bindingA, bindingC)).build();

        final var stateDiff = new ClusterStateDiff(before, after);

        assertEquals(Collections.emptySet(), stateDiff.addedAclEntries);
        assertEquals(Collections.emptySet(), stateDiff.deletedAclEntries);
        assertEquals(Set.of(bindingC), stateDiff.addedRbacBindings);
        assertEquals(Set.of(bindingB), stateDiff.deletedRbacBindings);
        assertEquals(Collections.emptySet(), stateDiff.deletedTopicNames);
        assertEquals(Collections.emptyMap(), stateDiff.addedTopics);
        assertEquals(Collections.emptyMap(), stateDiff.updatedTopicConfigs);
    }

    @Test
    public void topicDiffTest() {
        final var topicADesc = new TopicDescription("topicA", Collections.emptyMap(), null);
        final var topicBDesc = new TopicDescription("topicB", Collections.emptyMap(), null);
        final var topicCDesc = new TopicDescription("topicC", Collections.emptyMap(), null);

        final ClusterState before = ClusterState.builder().withTopicDescriptions(
                Map.of("topicA", topicADesc, "topicB", topicBDesc)
        ).build();

        final ClusterState after = ClusterState.builder().withTopicDescriptions(
                Map.of("topicA", topicADesc, "topicC", topicCDesc)
        ).build();

        final var stateDiff = new ClusterStateDiff(before, after);
        assertEquals(Collections.emptySet(), stateDiff.addedAclEntries);
        assertEquals(Collections.emptySet(), stateDiff.deletedAclEntries);
        assertEquals(Collections.emptySet(), stateDiff.addedRbacBindings);
        assertEquals(Collections.emptySet(), stateDiff.deletedRbacBindings);

        assertEquals(Set.of("topicB"), stateDiff.deletedTopicNames);
        assertEquals(Map.of("topicC", topicCDesc), stateDiff.addedTopics);
        assertEquals(Collections.emptyMap(), stateDiff.updatedTopicConfigs);
    }

    @Test
    public void updatedTopicConfigTest() {
        final var topicADescOld = new TopicDescription("topicA", Collections.emptyMap(), null);
        final var topicADescNew = new TopicDescription("topicA", Map.of("retention.ms", "2000"), null);

        final ClusterState before = ClusterState.builder().withTopicDescriptions(
                Map.of("topicA", topicADescOld)
        ).build();

        final ClusterState after = ClusterState.builder().withTopicDescriptions(
                Map.of("topicA", topicADescNew)
        ).build();

        final var stateDiff = new ClusterStateDiff(before, after);
        assertEquals(Collections.emptySet(), stateDiff.addedAclEntries);
        assertEquals(Collections.emptySet(), stateDiff.deletedAclEntries);
        assertEquals(Collections.emptySet(), stateDiff.addedRbacBindings);
        assertEquals(Collections.emptySet(), stateDiff.deletedRbacBindings);

        assertEquals(Collections.emptySet(), stateDiff.deletedTopicNames);
        assertEquals(Collections.emptyMap(), stateDiff.addedTopics);
        assertEquals(Map.of("topicA", Update.of(topicADescOld, topicADescNew)), stateDiff.updatedTopicConfigs);
    }
}