package net.christophschubert.kafka.clusterstate.formats.ktb;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.TopicDescription;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KtbTopologyConverter {
    interface TopicNameStrategy {
        String topicName(KtbTopology topology, KtbProject project, KtbTopic topic);
    }

    static class BoringTopicNameStrategy implements TopicNameStrategy {

        private final String separator;

        BoringTopicNameStrategy(String separator) {
            this.separator = separator;
        }

        @Override
        public String topicName(KtbTopology topology, KtbProject project, KtbTopic topic) {
            return topology.getContext() + separator + project.getName() + separator + topic.getName();
        }
    }
    interface AclStrategy {
        List<ACLEntry> createEntriesForProject();
    }

    static class EmptyAclStrategy implements AclStrategy {

        @Override
        public List<ACLEntry> createEntriesForProject() {
            return null;
        }
    }

    public ClusterState compile(KtbTopology topology, TopicNameStrategy topicNameStrategy, AclStrategy aclStrategy) {
        final Map<String, TopicDescription> collect = topology.getProjects().stream().flatMap(project ->
            project.topics.stream().map(topic -> new TopicDescription(
                    topicNameStrategy.topicName(topology, project,topic), topic.getConfig()))
        ).collect(Collectors.toMap(td -> td.name(), td -> td));
        return new ClusterState(Collections.EMPTY_SET, Collections.EMPTY_SET, collect);
    }
}
