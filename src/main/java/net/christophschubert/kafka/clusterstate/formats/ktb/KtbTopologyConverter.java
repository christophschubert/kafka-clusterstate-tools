package net.christophschubert.kafka.clusterstate.formats.ktb;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.TopicDescription;
import org.apache.kafka.common.protocol.types.Field;

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
        List<ACLEntry> createEntriesForClientInProject(C client, KtbProject project, TopicNameStrategy strategy);
    }

    static class EmptyAclStrategy implements AclClientStrategy {
        @Override
        public List<ACLEntry> createEntriesForClientInProject(KtbClient client, KtbProject project, TopicNameStrategy namingStrategy) {
            return Collections.EMPTY_LIST;
        }

        private static final EmptyAclStrategy theStrategy = new EmptyAclStrategy();
        static AclClientStrategy get() {
            return theStrategy;
        }
    }

    interface AclStrategy {
        List<ACLEntry> calculateAcls(KtbProject project, TopicNameStrategy nameStrategy);
    }

    static class BaseAclStrategy implements  AclStrategy{
        //another, generic option would be
        //public <C extends KtbClient> AclClientStrategy<C> getStrategyForClientClass(Class<C> clazz) {
        //}
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
        public List<ACLEntry> calculateAcls(KtbProject project, TopicNameStrategy nameStrategy) {
            List<ACLEntry> entries = new ArrayList<>();
            project.producers.forEach(producer ->
                    entries.addAll(producerStrategy().createEntriesForClientInProject(producer, project, nameStrategy))
            );
            project.consumers.forEach(consumer ->
                    entries.addAll(consumerStrategy().createEntriesForClientInProject(consumer, project, nameStrategy))
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

    FunctionalAclStrategy replicateKtb = new FunctionalAclStrategy(
            (consumer, project, nameStrategy) -> {
                return Collections.EMPTY_LIST;
            },
            (consumer, project, nameStrategy) -> {
                return Collections.EMPTY_LIST;
            },
            (consumer, project, nameStrategy) -> {
                return Collections.EMPTY_LIST;
            }
        );

    public ClusterState compile(KtbTopology topology, TopicNameStrategy topicNameStrategy, AclStrategy aclStrategy) {
        final Map<String, TopicDescription> collect = topology.getProjects().stream().flatMap(project ->
            project.topics.stream().map(topic -> new TopicDescription(
                    topicNameStrategy.topicName(topology, project, topic), topic.getConfig()))
        ).collect(Collectors.toMap(td -> td.name(), td -> td));
        final Set<ACLEntry> aclEntries = topology.getProjects().stream()
                .flatMap(ktbProject -> aclStrategy.calculateAcls(ktbProject, topicNameStrategy).stream())
                .collect(Collectors.toSet());
        // TODO: implement RBAC
        return new ClusterState(aclEntries, Collections.EMPTY_SET, collect);
    }

    public ClusterState compileReplicatingKTB(KtbTopology topology) {
        return compile(topology, new MightCauseCollisionTopicNameStrategy("_"), replicateKtb);
    }
}
