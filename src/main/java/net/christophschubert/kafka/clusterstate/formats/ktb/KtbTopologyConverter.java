package net.christophschubert.kafka.clusterstate.formats.ktb;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.AclEntries;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.TopicDescription;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class KtbTopologyConverter {
    interface TopicNameStrategy {
        String topicName(KtbTopology topology, KtbProject project, KtbTopic topic);
        String projectPrefix(KtbTopology topology, KtbProject project);
    }

    static class BoringTopicNameStrategy implements TopicNameStrategy {

        private final String separator;

        BoringTopicNameStrategy(String separator) {
            this.separator = separator;
        }

        @Override
        public String projectPrefix(KtbTopology topology, KtbProject project) {
            return topology.getContext() + separator + project.getName() + separator;
        }

        @Override
        public String topicName(KtbTopology topology, KtbProject project, KtbTopic topic) {
            return  projectPrefix(topology, project) + topic.getName();
        }
    }

    static class MightCauseCollisionTopicNameStrategy implements TopicNameStrategy {

        private final String separator;

        MightCauseCollisionTopicNameStrategy(String separator) {
            this.separator = separator;
        }

        @Override
        public String projectPrefix(KtbTopology topology, KtbProject project) {
            return topology.getContext() + separator + project.getName() ;
        }

        @Override
        public String topicName(KtbTopology topology, KtbProject project, KtbTopic topic) {
            return  projectPrefix(topology, project) + separator + topic.getName();
        }
    }

    interface AclClientStrategy<C extends KtbClient> {
        Set<ACLEntry> createEntriesForClientInProject(C client, KtbTopology topology, KtbProject project, TopicNameStrategy nameStrategy);
    }

    static class EmptyAclStrategy<C extends KtbClient> implements AclClientStrategy<C> {
        private static final EmptyAclStrategy theStrategy = new EmptyAclStrategy();
        static AclClientStrategy get() {
            return theStrategy;
        }


        @Override
        public Set<ACLEntry> createEntriesForClientInProject(C client, KtbTopology topology, KtbProject project, TopicNameStrategy strategy) {
            return Collections.EMPTY_SET;
        }
    }

    interface AclStrategy {
        Set<ACLEntry> calculateAcls(KtbTopology topology, KtbProject project, TopicNameStrategy nameStrategy);
    }

    static class BaseAclStrategy implements  AclStrategy{

        AclClientStrategy<KtbConsumer> consumerStrategy() {
            return EmptyAclStrategy.get();
        };
        AclClientStrategy<KtbProducer> producerStrategy() {
            return EmptyAclStrategy.get();
        };
        AclClientStrategy<KtbConnector> connectorStrategy() {
            return EmptyAclStrategy.get();
        }

        @Override
        public Set<ACLEntry> calculateAcls(KtbTopology topology, KtbProject project, TopicNameStrategy nameStrategy ) {
            Set<ACLEntry> entries = new HashSet<>();
            project.producers.forEach(producer ->
                    entries.addAll(producerStrategy().createEntriesForClientInProject(producer, topology, project, nameStrategy))
            );
            project.consumers.forEach(consumer ->
                    entries.addAll(consumerStrategy().createEntriesForClientInProject(consumer, topology, project, nameStrategy))
            );
            return entries;
        }
    }

    static class FunctionalAclStrategy extends BaseAclStrategy {

        private final AclClientStrategy<KtbConsumer> consumerStrategy;
        private final AclClientStrategy<KtbConnector> connectorStrategy;
        private final AclClientStrategy<KtbProducer> producerStrategy;

        public FunctionalAclStrategy(
                AclClientStrategy<KtbConsumer> consumerStrategy,
                AclClientStrategy<KtbConnector> connectorStrategy,
                AclClientStrategy<KtbProducer> producerStrategy) {
            this.consumerStrategy = consumerStrategy;
            this.connectorStrategy = connectorStrategy;
            this.producerStrategy = producerStrategy;
        }

        @Override
        public AclClientStrategy<KtbConnector> connectorStrategy() {
            return connectorStrategy;
        }

        @Override
        public AclClientStrategy<KtbConsumer> consumerStrategy() {
            return consumerStrategy;
        }

        @Override
        public AclClientStrategy<KtbProducer> producerStrategy() {
            return producerStrategy;
        }
    }

    static FunctionalAclStrategy replicateKtb = new FunctionalAclStrategy(
            (consumer, topology, project, nameStrategy) ->
                project.getTopics().stream().flatMap(topic -> {
                    final String fullTopicName = nameStrategy.topicName(topology, project, topic);
                    final String groupId = consumer.groupId;
                    if (StringUtils.isEmpty(groupId)) {
                        return AclEntries.topicLiteralConsumerWildcard(consumer.principal, fullTopicName).stream();
                    } else {
                        return AclEntries.topicLiteralConsumer(consumer.principal, fullTopicName, groupId).stream();
                    }
                }).collect(Collectors.toSet())
            ,
            //TODO implement logic for connector
            (connector, topology, project, nameStrategy) -> Collections.EMPTY_SET,
            (producer, topology, project, nameStrategy) ->
                    project.getTopics().stream().flatMap(topic -> {
                        final String fullTopicName = nameStrategy.topicName(topology, project, topic);
                        return AclEntries.topicLiteralProducer(producer.getPrincipal(), fullTopicName).stream();
                    }).collect(Collectors.toSet())
        );

    static FunctionalAclStrategy usePrefixedTopicsAcls = new FunctionalAclStrategy(
            (consumer, topology, project, nameStrategy) -> {
                final String topicPrefix = nameStrategy.projectPrefix(topology, project);
                final String groupId = consumer.getGroupId();
                if (StringUtils.isEmpty(groupId)) {
                    return AclEntries.topicPrefixConsumerWildcard(consumer.getPrincipal(), topicPrefix);
                } else {
                    return AclEntries.topicPrefixConsumer(consumer.getPrincipal(), topicPrefix, groupId);
                }
            },
            //TODO: implement Connector ACLs
            (connector, topology, project, nameStrategy) -> Collections.EMPTY_SET,
            (producer, topology, project, nameStrategy) -> {
                final String topicPrefix = nameStrategy.projectPrefix(topology, project);
                return AclEntries.topicPrefixProducer(producer.getPrincipal(), topicPrefix);
            }
    );

    public ClusterState compile(KtbTopology topology, TopicNameStrategy topicNameStrategy, AclStrategy aclStrategy) {
        final Map<String, TopicDescription> collect = topology.getProjects().stream()
                .flatMap(project -> project.topics.stream().map(topic -> {
                    final String fullTopicName = topicNameStrategy.topicName(topology, project, topic);
                    return new TopicDescription(fullTopicName, topic.getConfig());
                }))
                .collect(Collectors.toMap(td -> td.name(), td -> td));

        final Set<ACLEntry> aclEntries = topology.getProjects().stream()
                .flatMap(ktbProject -> aclStrategy.calculateAcls(topology, ktbProject, topicNameStrategy).stream())
                .collect(Collectors.toSet());
        // TODO: implement RBAC
        return new ClusterState(aclEntries, Collections.EMPTY_SET, collect);
    }

    public ClusterState compileReplicatingKTB(KtbTopology topology) {
        return compile(topology, new MightCauseCollisionTopicNameStrategy("_"), replicateKtb);
    }

    public ClusterState compileWithPrefixAcls(KtbTopology topology) {
        return compile(topology, new MightCauseCollisionTopicNameStrategy("_"), usePrefixedTopicsAcls);
    }
}
