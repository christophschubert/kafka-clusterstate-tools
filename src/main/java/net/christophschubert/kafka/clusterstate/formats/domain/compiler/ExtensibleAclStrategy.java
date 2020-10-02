package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.formats.domain.*;

import java.util.HashSet;
import java.util.Set;

class ExtensibleAclStrategy implements DomainCompiler.AclStrategy {

    public interface SubAclStrategy<R extends ProjectSubResource> {
        Set<ACLEntry> acls(R resource, DomainCompiler.ResourceNamingStrategy namingStrategy);
    }

    private final SubAclStrategy<Consumer> consumerSubAclStrategy;
    private final SubAclStrategy<Producer> producerSubAclStrategy;
    private final SubAclStrategy<StreamsApp> streamsAppSubAclStrategy;

    ExtensibleAclStrategy(
            SubAclStrategy<Consumer> consumerSubAclStrategy,
            SubAclStrategy<Producer> producerSubAclStrategy,
            SubAclStrategy<StreamsApp> streamsAppSubAclStrategy
    ) {
        this.consumerSubAclStrategy = consumerSubAclStrategy;
        this.producerSubAclStrategy = producerSubAclStrategy;
        this.streamsAppSubAclStrategy = streamsAppSubAclStrategy;
    }

    /**
     * Sets the ACLs for the resources in a project by visiting each sub-resource.
     *
     * @param project the Project to compute ACLs
     * @param namingStrategy strategy to use for consumer groups, topic names, etc.
     * @return A set of ACLs which is the union of the ones specified by the sub-resource strategies.
     */
    @Override
    public Set<ACLEntry> aclsForProject(Project project, DomainCompiler.ResourceNamingStrategy namingStrategy) {
        Set<ACLEntry> entries = new HashSet<>();
        project.consumers.forEach(consumer -> entries.addAll(
                consumerSubAclStrategy.acls(consumer,  namingStrategy)
        ));
        project.producers.forEach(producer -> entries.addAll(
                producerSubAclStrategy.acls(producer,  namingStrategy)
        ));
        project.streamsApps.forEach(streamsApp -> entries.addAll(
                streamsAppSubAclStrategy.acls(streamsApp,  namingStrategy)
        ));
        return entries;
    }
}
