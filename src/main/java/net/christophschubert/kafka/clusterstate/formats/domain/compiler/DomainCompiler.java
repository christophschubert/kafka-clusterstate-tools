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
            return projectPrefix(resource.parent) + resource.id();
        }

        String projectPrefix(Project project);
    }

    public static class BoringStrategy implements ResourceNamingStrategy {

        @Override
        public String projectPrefix(Project project) {
            return project.parent.name() + "_" + project.name + "_";
        }

    }

    interface AclStrategy {
        Set<ACLEntry> aclsForProject(Project project, ResourceNamingStrategy namingStrategy);
    }

    /**
     * Convert a Domain description to a (desired) ClusterState.
     *
     * @param domain the domain to compile
     * @param namingStrategy how to name resources
     * @param aclStrategy how to set ACLs
     * @return A ClusterState representing the Domain.
     */
    public ClusterState compile(Domain domain, ResourceNamingStrategy namingStrategy, AclStrategy aclStrategy) {

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
