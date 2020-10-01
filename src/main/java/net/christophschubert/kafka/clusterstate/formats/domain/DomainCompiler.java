package net.christophschubert.kafka.clusterstate.formats.domain;

import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.TopicDescription;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class DomainCompiler {
    interface TopicNamingStrategy {
        String name(Topic topic);
    }

    public static class BoringStrategy implements TopicNamingStrategy {

        @Override
        public String name(Topic topic) {
            return topic.parent.parent.name() + "_" + topic.parent.name + "_" + topic.name;
        }
    }

    interface AclStrategy {

    }

    public ClusterState compile(Domain domain, TopicNamingStrategy namingStrategy, AclStrategy aclStrategy) {

        final Map<String, TopicDescription> topics = domain.projects.stream()
                .flatMap(project -> project.topics.stream())
                .collect(Collectors.toMap(
                        namingStrategy::name,
                        topic -> new TopicDescription(namingStrategy.name(topic), topic.configs)
                ));

        return new ClusterState(Collections.EMPTY_SET, Collections.EMPTY_SET, topics);
    }
}
