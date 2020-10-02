package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.ACLEntry;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.TopicDescription;
import net.christophschubert.kafka.clusterstate.formats.domain.Domain;
import net.christophschubert.kafka.clusterstate.formats.domain.Project;
import net.christophschubert.kafka.clusterstate.formats.domain.ProjectSubResource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DomainCompiler {

    public DomainCompiler(ResourceNamingStrategy namingStrategy, AclStrategy aclStrategy) {
        this.namingStrategy = namingStrategy;
        this.aclStrategy = aclStrategy;
    }

    /**
     * Used to generate fully qualified names for topics, consumer groups as well
     * we project-wide names for prefixed ACLs and role-bindings.
     *
     * Every implementation must fulfill: name(resource).startsWith(projectPrefix(project)) for
     * each resource which is part of project.
     *
     * This invariant holds with the default implementation of name.
     */
    interface ResourceNamingStrategy {
        default String name(ProjectSubResource resource) {
            return name(resource.parent, resource.id());
        }

        default String name(Project project, String resourceName) {
            return projectPrefix(project) + resourceName;
        }

        String projectPrefix(Project project);
    }


    interface AclStrategy {
        Set<ACLEntry> aclsForProject(Project project, ResourceNamingStrategy namingStrategy);
    }

    private final ResourceNamingStrategy namingStrategy;
    private final AclStrategy aclStrategy;

    /**
     * Convert a Domain description to a (desired) ClusterState.
     *
     * @param domain the domain to compile
     * @return A ClusterState representing the Domain.
     */
    public ClusterState compile(Domain domain) {

        final Map<String, TopicDescription> topics = domain.projects.stream()
                .flatMap(project -> project.topics.stream())
                .collect(Collectors.toMap(
                        namingStrategy::name,
                        topic -> new TopicDescription(namingStrategy.name(topic), topic.configs)
                ));

        final var acls = domain.projects.stream()
                .flatMap(project -> aclStrategy.aclsForProject(project, namingStrategy).stream())
                .collect(Collectors.toSet());

        //TODO: add code to generate RBAC role bindings
        return new ClusterState(acls, Collections.emptySet(), topics);
    }
}
