package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.AclEntries;
import net.christophschubert.kafka.clusterstate.formats.domain.*;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.resource.ResourceType;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultStrategies {

    public static final ResourceNamingStrategy namingStrategy = new DefaultNamingStrategy();

    public static class DefaultNamingStrategy implements ResourceNamingStrategy {

        public static final String DEFAULT_SEPARATOR = "_";

        private final String separator;

        public DefaultNamingStrategy(String separator) {
            this.separator = separator;
        }

        public DefaultNamingStrategy() {
            this(DEFAULT_SEPARATOR);
        }

        @Override
        public String projectPrefix(@NotNull Project project) {
            return project.parent.name() + separator + project.name + separator;
        }
    }


    /**
     * The default ACL strategy, represents best practices and tries to use minimal ACLs.
     *
     * Could be used as a starting point for different ACL assignment.
     */
    public static final ExtensibleProjectAuthorizationStrategy<ACLEntry> aclStrategy = new ExtensibleProjectAuthorizationStrategy<>(
            new ConsumerAclStrategy(),
            new DefaultProducerAclStrategy(),
            new DefaultStreamsAppAclStrategy(),
            new DefaultTopicForeignAclStrategy()
    );

    /**
     * Placeholder implementation which return an empty ACL set.
     *
     * @param <A> type of the access control entry assigned
     * @param <R> type of the resource
     */
    static class EmptyAclStrategy<A, R extends ProjectSubResource> implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<A, R> {
        @Override
        public Set<A> acls(R resource, ResourceNamingStrategy namingStrategy) {
            return Collections.emptySet();
        }
    }

    //TODO: add descriptions of produced ACLs
    //TODO: rename!
    static class ConsumerAclStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<ACLEntry, Consumer> {
        @Override
        public Set<ACLEntry> acls(Consumer consumer, ResourceNamingStrategy namingStrategy) {
            final Project project = consumer.parent;
            final String groupId = namingStrategy.name(consumer);
            return readAcls(consumer.principal, project, groupId, consumer.prefixGroup, consumer.topics, namingStrategy);
        }

    }

    //TODO: add descriptions of produced ACLs
    static class DefaultProducerAclStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<ACLEntry, Producer> {

        @Override
        public Set<ACLEntry> acls(Producer producer, ResourceNamingStrategy namingStrategy) {
            final Project project = producer.parent;
            return writeAcls(producer.principal, project, producer.topics, namingStrategy);

            //TODO: add rights to access transactionalId if specified
        }
    }

    static class DefaultTopicForeignAclStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<ACLEntry, Topic> {

        @Override
        public Set<ACLEntry> acls(Topic topic, ResourceNamingStrategy namingStrategy) {
            final var topicName = namingStrategy.name(topic);
            final var acls = new HashSet<ACLEntry>();
            topic.producerPrincipals.forEach(
                    principal -> acls.add(AclEntries.allowAnyHostLiteral(AclOperation.WRITE, principal, topicName, ResourceType.TOPIC))
            );
            topic.consumerPrincipals.forEach(
                    principal -> acls.add(AclEntries.allowAnyHostLiteral(AclOperation.READ, principal, topicName, ResourceType.TOPIC))
            );
            return acls;
        }
    }

    /**
     *
     */
    static class DefaultStreamsAppAclStrategy implements ExtensibleProjectAuthorizationStrategy.ResourceStrategy<ACLEntry, StreamsApp> {

        @Override
        public Set<ACLEntry> acls(StreamsApp streamsApp, ResourceNamingStrategy namingStrategy) {
            final var principal = streamsApp.principal;
            final var project = streamsApp.parent;
            final String qualifiedName = namingStrategy.name(streamsApp);

            //first make app producer and consumer of all topics (prefixed)
            final Set<ACLEntry> aclEntries = new HashSet<>();
            aclEntries.addAll(writeAcls(principal, project, streamsApp.outputTopics, namingStrategy));
            aclEntries.addAll(readAcls(principal, project, qualifiedName, streamsApp.prefixApplicationId, streamsApp.inputTopics, namingStrategy));


            if (streamsApp.prefixApplicationId) {
                //add prefix ACLs for transaction ID
                aclEntries.add(AclEntries.allowAnyHostPrefix(AclOperation.DESCRIBE, principal, qualifiedName, ResourceType.TRANSACTIONAL_ID));
                aclEntries.add(AclEntries.allowAnyHostPrefix(AclOperation.WRITE, principal, qualifiedName, ResourceType.TRANSACTIONAL_ID));
            } else {
                aclEntries.add(AclEntries.allowAnyHostLiteral(AclOperation.DESCRIBE, principal, qualifiedName, ResourceType.TRANSACTIONAL_ID));
                aclEntries.add(AclEntries.allowAnyHostLiteral(AclOperation.WRITE, principal, qualifiedName, ResourceType.TRANSACTIONAL_ID));
            }
            //give prefixed rights for all internal topics. These will be generated with the applicationId as prefix
            final var internalTopicAcls = Stream.of(AclOperation.READ, AclOperation.DELETE, AclOperation.WRITE, AclOperation.CREATE)
                    .map(op -> AclEntries.allowAnyHostPrefix(op, principal, qualifiedName, ResourceType.TOPIC))
                    .collect(Collectors.toSet());
            aclEntries.addAll(internalTopicAcls);
            // according to
            // https://docs.confluent.io/current/streams/developer-guide/security.html
            // we still need to add DESCRIBE on the consumer group: need to check this!
            return aclEntries;
        }
    }


    static Set<ACLEntry> writeAcls(String principal, Project project, Set<String> topicNames, ResourceNamingStrategy namingStrategy) {
        if (topicNames.isEmpty()) {
            return AclEntries.topicPrefixProducer(principal, namingStrategy.projectPrefix(project));
        } else {
            return topicNames.stream().flatMap(topicName ->
                    AclEntries.topicLiteralProducer(principal, namingStrategy.name(project, topicName)).stream()
            ).collect(Collectors.toSet());
        }
    }


    static Set<ACLEntry> readAcls(String principal, Project project, String groupId, boolean isPrefixGroup, Set<String> topicNames, ResourceNamingStrategy namingStrategy) {
        if (topicNames.isEmpty()) {
            return AclEntries.topicPrefixConsumer(principal, namingStrategy.projectPrefix(project), groupId, isPrefixGroup);
        } else {
            // we have a non-empty list of topics => create ACLs-entries for each of them
            return topicNames.stream().flatMap(topicName ->
                    AclEntries.topicLiteralConsumer(principal, namingStrategy.name(project, topicName), groupId, isPrefixGroup).stream())
                    .collect(Collectors.toSet());
        }
    }
}
