package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.AclEntries;
import net.christophschubert.kafka.clusterstate.formats.domain.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultStrategies {

    public static final DomainCompiler.ResourceNamingStrategy namingStrategy = new BoringStrategy();

    public static class BoringStrategy implements DomainCompiler.ResourceNamingStrategy {
        @Override
        public String projectPrefix(Project project) {
            return project.parent.name() + "_" + project.name + "_";
        }
    }


    /**
     * The default ACL strategy, represents best practices and tries to use minimal ACLs.
     *
     * Could be used as a starting point for different ACL assignment.
     */
    public static final ExtensibleAclStrategy aclStrategy = new ExtensibleAclStrategy(
            new ConsumerAclStrategy(),
            new DefaultProducerAclStrategy(),
            new DefaultStreamsAppAclStrategy()
    );

    /**
     * Placeholder implementation which return an empty ACL set.
     *
     * @param <R> type of the Resource
     */
    static class EmptyAclStrategy<R extends ProjectSubResource> implements ExtensibleAclStrategy.ResourceAclStrategy<R> {

        @Override
        public Set<ACLEntry> acls(R resource, DomainCompiler.ResourceNamingStrategy namingStrategy) {
            return Collections.emptySet();
        }
    }

    //TODO: add descriptions of produced ACLs
    static class ConsumerAclStrategy implements ExtensibleAclStrategy.ResourceAclStrategy<Consumer> {
        @Override
        public Set<ACLEntry> acls(Consumer consumer, DomainCompiler.ResourceNamingStrategy namingStrategy) {
            final Project project = consumer.parent;
            final String groupId = namingStrategy.name(consumer);

            if (consumer.topics.isEmpty()) {
                return AclEntries.topicPrefixConsumer(consumer.principal, namingStrategy.projectPrefix(project), groupId, consumer.prefixGroup);
            } else {
                // we have a non-empty list of topics => create ACLs-entries for each of them
                return consumer.topics.stream().flatMap(topicName ->
                        AclEntries.topicLiteralConsumer(consumer.principal, namingStrategy.name(project, topicName), groupId, consumer.prefixGroup).stream())
                        .collect(Collectors.toSet());

            }
        }
    }

    //TODO: add descriptions of produced ACLs
    static class DefaultProducerAclStrategy implements ExtensibleAclStrategy.ResourceAclStrategy<Producer> {

        @Override
        public Set<ACLEntry> acls(Producer producer, DomainCompiler.ResourceNamingStrategy namingStrategy) {
            final Project project = producer.parent;
            if (producer.topics.isEmpty()) {
                return AclEntries.topicPrefixProducer(producer.principal, namingStrategy.projectPrefix(project));
            } else {
                return producer.topics.stream().flatMap(topicName ->
                        AclEntries.topicLiteralProducer(producer.principal, namingStrategy.name(project, topicName)).stream()
                ).collect(Collectors.toSet());
            }
        }
    }

    //TODO: add description of produced ACLs
    static class DefaultStreamsAppAclStrategy implements ExtensibleAclStrategy.ResourceAclStrategy<StreamsApp> {

        @Override
        public Set<ACLEntry> acls(StreamsApp streamsApp, DomainCompiler.ResourceNamingStrategy namingStrategy) {
            final var principal = streamsApp.principal;
            final var projectPrefix = namingStrategy.projectPrefix(streamsApp.parent);
            final Set<ACLEntry> aclEntries = new HashSet<>();

            //first make app producer and consumer of all topics (prefixed)
            aclEntries.addAll( AclEntries.topicPrefixProducer(principal, projectPrefix) );
            aclEntries.addAll( AclEntries.topicPrefixConsumer(principal, projectPrefix, streamsApp.applicationId));

            //TODO: think about which additional rights we need?
            //  - transactional ID?
            //  - rights to create/access internal topics -- for this we should also export
            //    the fully qualified application ID.

            return aclEntries;
        }
    }

}
