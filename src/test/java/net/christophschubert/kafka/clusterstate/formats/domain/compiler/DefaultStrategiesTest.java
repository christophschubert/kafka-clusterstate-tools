package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.formats.domain.*;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DefaultStrategiesTest {
    private final ResourceNamingStrategy namingStrategy = new DefaultStrategies.DefaultNamingStrategy();

    @Test
    public void singleProducerTest() {
        final Producer producer =  new Producer("User:abc", null, false, null);
        final Project pSimple = Project.builder("projectA")
                .addProducer(producer)
                .addTopic("topicA")
                .addTopic("topicB")
                .build();
        final Domain domain = new Domain("domainX", Set.of(pSimple));

        final var aclStrategy = new DefaultStrategies.DefaultProducerAclStrategy();
        final var acls = aclStrategy.acls(producer, namingStrategy);
        assertEquals(Set.of(
                new ACLEntry(AclOperation.WRITE, "User:abc", "*", AclPermissionType.ALLOW, "domainX_projectA_", ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, "User:abc", "*", AclPermissionType.ALLOW, "domainX_projectA_", ResourceType.TOPIC, PatternType.PREFIXED)
        ), acls);
    }

    @Test
    public void singleProducerTransactionalIdTest() {
        final Producer producer =  new Producer("User:abc", "atransid", false, null);
        final Project pSimple = Project.builder("projectA")
                .addProducer(producer)
                .addTopic("topicA")
                .addTopic("topicB")
                .build();
        final Domain domain = new Domain("domainX", Set.of(pSimple));

        final var aclStrategy = new DefaultStrategies.DefaultProducerAclStrategy();
        final var acls = aclStrategy.acls(producer, namingStrategy);
        assertEquals(Set.of(
                new ACLEntry(AclOperation.WRITE, "User:abc", "*", AclPermissionType.ALLOW, "domainX_projectA_", ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, "User:abc", "*", AclPermissionType.ALLOW, "domainX_projectA_", ResourceType.TOPIC, PatternType.PREFIXED)
                // TODO: add operations for transactional ID (check docs first)
        ), acls);
    }

    @Test
    public void consumerTest() {
        final var literalPrincipal = "User:literal";
        final var prefixPrincipal = "User:prefix";

        final Consumer consumerLiteralGroup =  new Consumer(literalPrincipal, "group_literal", false, null);
        final Consumer consumerPrefixGroup =  new Consumer(prefixPrincipal, "group_prefix", true, null);
        final Project pSimple = Project.builder("projectA")
                .addConsumer(consumerLiteralGroup)
                .addConsumer(consumerPrefixGroup)
                .addTopic("topicA")
                .addTopic("topicB")
                .build();
        final Domain domain = new Domain("domainX", Set.of(pSimple));

        final var projectPrefix = "domainX_projectA_";

        final var aclStrategy = new DefaultStrategies.ConsumerAclStrategy();
        final var aclsLiteral = aclStrategy.acls(consumerLiteralGroup, namingStrategy);
        assertEquals(Set.of(
                new ACLEntry(AclOperation.READ, literalPrincipal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, literalPrincipal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, literalPrincipal, "*", AclPermissionType.ALLOW, projectPrefix + "group_literal", ResourceType.GROUP, PatternType.LITERAL)
        ), aclsLiteral);

        assertEquals(Set.of(
                new ACLEntry(AclOperation.READ, prefixPrincipal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, prefixPrincipal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, prefixPrincipal, "*", AclPermissionType.ALLOW, projectPrefix + "group_prefix", ResourceType.GROUP, PatternType.PREFIXED)
        ), aclStrategy.acls(consumerPrefixGroup, namingStrategy));
    }

    @Test
    public void producerRestrictedTopics() {
        final String principal = "User:abc";
        final var producer =  new Producer(principal, null, false, Set.of("topicA", "topicB"));
        final var project = Project.builder("projectA")
                .addProducer(producer)
                .addTopic("topicA")
                .build();
        final Domain domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = "domainX_projectA_";

        final var aclStrategy = new DefaultStrategies.DefaultProducerAclStrategy();
        assertEquals(Set.of(
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicB", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicB", ResourceType.TOPIC, PatternType.LITERAL)
        ), aclStrategy.acls(producer, namingStrategy));
    }

    @Test
    public void consumerRestrictedTopics() {
        final String principal = "User:abc";
        final String groupId = "group_id";
        final var consumer = new Consumer(principal, groupId, false, Set.of("topicA", "topicB"));
        final var project = Project.builder("projectA")
                .addConsumer(consumer)
                .build();

        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var aclStrategy = new DefaultStrategies.ConsumerAclStrategy();
        assertEquals(Set.of(
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicB", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicB", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix + groupId, ResourceType.GROUP, PatternType.LITERAL)
        ), aclStrategy.acls(consumer, namingStrategy));
    }

    @Test
    public void foreignConsumer() {
        final var principal = "User:other";
        final var topicA = new Topic("topicA", null, null, Set.of(principal), null);
        final var project = Project.builder("projectA")
                .addTopic(topicA)
                .build();
        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var aclStrategy = new DefaultStrategies.DefaultTopicForeignAclStrategy();
        assertEquals(Set.of(
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL)
        ), aclStrategy.acls(topicA, namingStrategy));
    }

    @Test
    public void foreignProducer() {
        final var principal = "User:other";
        final var topicA = new Topic("topicA", null, null, null, Set.of(principal));
        final var project = Project.builder("projectA")
                .addTopic(topicA)
                .build();
        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var aclStrategy = new DefaultStrategies.DefaultTopicForeignAclStrategy();
        assertEquals(Set.of(
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL)
        ), aclStrategy.acls(topicA, namingStrategy));
    }

    @Test
    public void foreignComplex() {
        final var principalA = "User:otherA";
        final var principalB = "User:otherB";
        final var principalC = "User:otherC";
        final var topicA = new Topic("topicA", null, null, Set.of(principalA, principalB), Set.of(principalB, principalC));
        final var project = Project.builder("projectA")
                .addTopic(topicA)
                .build();
        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var aclStrategy = new DefaultStrategies.DefaultTopicForeignAclStrategy();
        assertEquals(Set.of(
                new ACLEntry(AclOperation.READ, principalA, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.READ, principalB, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.WRITE, principalB, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.WRITE, principalC, "*", AclPermissionType.ALLOW, projectPrefix + "topicA", ResourceType.TOPIC, PatternType.LITERAL)

        ), aclStrategy.acls(topicA, namingStrategy));
    }

    @Test
    public void streamsApp() {
        final String principal = "User:a";
        final var app = new StreamsApp("app_id", "User:a", false, null, null);
        final var project = Project.builder("projectA")
                .addTopic("topicA")
                .addStreamsApp(app)
                .build();
        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var name = namingStrategy.name(app);
        final var aclStrategy = new DefaultStrategies.DefaultStreamsAppAclStrategy();
        assertEquals(Set.of(

                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),

                //internal topics
                //TODO: in this case READ and WRITE are superfluous
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.CREATE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DELETE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),

                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, name, ResourceType.GROUP, PatternType.LITERAL),

                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TRANSACTIONAL_ID, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TRANSACTIONAL_ID, PatternType.LITERAL)
                //TODO: check whether describe is needed
        ), aclStrategy.acls(app, namingStrategy));
    }

    @Test
    public void streamsAppPrefixedAppId() {
        final String principal = "User:a";
        final var app = new StreamsApp("app_id", "User:a", true, null, null);
        final var project = Project.builder("projectA")
                .addTopic("topicA")
                .addStreamsApp(app)
                .build();

        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var name = namingStrategy.name(app);
        final var aclStrategy = new DefaultStrategies.DefaultStreamsAppAclStrategy();
        assertEquals(Set.of(

                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix, ResourceType.TOPIC, PatternType.PREFIXED),

                //internal topics
                //TODO: in this case READ and WRITE are superfluous
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.CREATE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DELETE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),

                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, name, ResourceType.GROUP, PatternType.PREFIXED),

                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TRANSACTIONAL_ID, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TRANSACTIONAL_ID, PatternType.PREFIXED)
            //TODO: check whether describe is needed
        ), aclStrategy.acls(app, namingStrategy));
    }

    @Test
    public void streamsAppRestrictedTopics() {
        final String principal = "User:a";
        final var app = new StreamsApp("app_id", "User:a", true, Set.of("inputA", "inputB"), Set.of("outputA", "outputB"));
        final var project = Project.builder("projectA")
                .addTopic("topicA")
                .addStreamsApp(app)
                .build();

        final var domain = new Domain("domainX", Set.of(project));
        final var projectPrefix = namingStrategy.projectPrefix(project);
        final var name = namingStrategy.name(app);
        final var aclStrategy = new DefaultStrategies.DefaultStreamsAppAclStrategy();
        assertEquals(Set.of(

                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "outputA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "outputB", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix + "inputA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, projectPrefix + "inputB", ResourceType.TOPIC, PatternType.LITERAL),

                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "outputA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "outputB", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "inputA", ResourceType.TOPIC, PatternType.LITERAL),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, projectPrefix + "inputB", ResourceType.TOPIC, PatternType.LITERAL),


                //internal topics
                //TODO: in this case READ and WRITE are superfluous
                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.CREATE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DELETE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),
                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TOPIC, PatternType.PREFIXED),

                new ACLEntry(AclOperation.READ, principal, "*", AclPermissionType.ALLOW, name, ResourceType.GROUP, PatternType.PREFIXED),

                new ACLEntry(AclOperation.WRITE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TRANSACTIONAL_ID, PatternType.PREFIXED),
                new ACLEntry(AclOperation.DESCRIBE, principal, "*", AclPermissionType.ALLOW, name, ResourceType.TRANSACTIONAL_ID, PatternType.PREFIXED)
                //TODO: check whether describe is needed
        ), aclStrategy.acls(app, namingStrategy));
    }

}