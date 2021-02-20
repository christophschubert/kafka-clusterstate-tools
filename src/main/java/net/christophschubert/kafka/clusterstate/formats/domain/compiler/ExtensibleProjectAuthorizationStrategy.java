package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.formats.domain.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a visitor pattern with the configurable sub-strategies for
 * consumer, producer, and streaming apps.
 */
public class ExtensibleProjectAuthorizationStrategy<A> implements DomainCompiler.ProjectAuthorizationStrategy {

    /**
     * Functional interface to compute the ACLs for a sub-resource (e.g., consumer, producer).
     * @param <A> type of access control generated.
     * @param <R> type of the resource.
     */
    public interface ResourceStrategy<A, R extends ProjectSubResource> {
        Set<A> acls(R resource, ResourceNamingStrategy namingStrategy);
    }

    private final ResourceStrategy<A, Consumer> consumerSubAclStrategy;
    private final ResourceStrategy<A, Producer> producerSubAclStrategy;
    private final ResourceStrategy<A, StreamsApp> streamsAppSubAclStrategy;
    private final ResourceStrategy<A, Topic> foreignAccessStrategy;

    public ExtensibleProjectAuthorizationStrategy(
            ResourceStrategy<A, Consumer> consumerSubAclStrategy,
            ResourceStrategy<A, Producer> producerSubAclStrategy,
            ResourceStrategy<A, StreamsApp> streamsAppSubAclStrategy,
            ResourceStrategy<A, Topic> foreignAccessStrategy
    ) {
        this.consumerSubAclStrategy = consumerSubAclStrategy;
        this.producerSubAclStrategy = producerSubAclStrategy;
        this.streamsAppSubAclStrategy = streamsAppSubAclStrategy;
        this.foreignAccessStrategy = foreignAccessStrategy;
    }

    /**
     * Sets the ACLs for the resources in a project by visiting each sub-resource.
     *
     * @param project the Project to compute ACLs
     * @param namingStrategy strategy to use for consumer groups, topic names, etc.
     * @return A set of ACLs which is the union of the ones specified by the sub-resource strategies.
     */
    @Override
    public Set<A> authForProject(Project project, ResourceNamingStrategy namingStrategy) {
        Set<A> entries = new HashSet<>();
        project.consumers.forEach(consumer -> entries.addAll(
                consumerSubAclStrategy.acls(consumer,  namingStrategy)
        ));
        project.producers.forEach(producer -> entries.addAll(
                producerSubAclStrategy.acls(producer,  namingStrategy)
        ));
        project.streamsApps.forEach(streamsApp -> entries.addAll(
                streamsAppSubAclStrategy.acls(streamsApp,  namingStrategy)
        ));
        project.topics.forEach(topic -> entries.addAll(
                foreignAccessStrategy.acls(topic, namingStrategy)
        ));
        return entries;
    }
}
