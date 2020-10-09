package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.*;
import net.christophschubert.kafka.clusterstate.formats.domain.DataModel;
import net.christophschubert.kafka.clusterstate.formats.domain.Domain;
import net.christophschubert.kafka.clusterstate.formats.domain.Project;
import net.christophschubert.kafka.clusterstate.formats.domain.ProjectSubResource;
import net.christophschubert.kafka.clusterstate.mds.RbacBindingInScope;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DomainCompiler {

    public DomainCompiler(
            ResourceNamingStrategy namingStrategy,
            ProjectAuthorizationStrategy<ACLEntry> aclStrategy,
            ProjectAuthorizationStrategy<RbacBindingInScope> rbacStrategy
    ) {
        this.namingStrategy = namingStrategy;
        this.aclStrategy = aclStrategy;
        this.roleBindingStrategy = rbacStrategy;
    }

    public static DomainCompiler createAcls(ResourceNamingStrategy namingStrategy, ProjectAuthorizationStrategy<ACLEntry> aclStrategy) {
        return new DomainCompiler(namingStrategy, aclStrategy, null);
    }

    public static DomainCompiler createRoleBindings(ResourceNamingStrategy namingStrategy, ProjectAuthorizationStrategy<RbacBindingInScope> rbacStrategy) {
        return new DomainCompiler(namingStrategy, null, rbacStrategy);
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


    interface ProjectAuthorizationStrategy<A> {
        Set<A> authForProject(Project project, ResourceNamingStrategy namingStrategy);
    }


    private final ResourceNamingStrategy namingStrategy;
    private final ProjectAuthorizationStrategy<RbacBindingInScope> roleBindingStrategy;
    private final ProjectAuthorizationStrategy<ACLEntry> aclStrategy;

    //TODO: refactor and document properly
    TopicDataModel convertDataModel(DataModel dm) {

        if (dm == null) {
            return new TopicDataModel(null, null);
        }
        //TODO: refactor this mess!
        SerializationInfo key = null;
        SerializationInfo value = null;
        if (dm.key != null) {
            key = new SerializationInfo(dm.key.type, dm.key.schemaFile);
        }
        if (dm.value != null) {
            value = new SerializationInfo(dm.value.type, dm.value.schemaFile);
        }
        return new TopicDataModel(key, value);
    }

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
                        topic -> new TopicDescription(namingStrategy.name(topic), topic.configs,
                                    convertDataModel(topic.dataModel))
                ));

        Set<ACLEntry> acls = Collections.emptySet();
        Set<RbacBindingInScope> bindings = Collections.emptySet();
        //TODO: extract logic to method
        if (roleBindingStrategy != null) {
            bindings = domain.projects.stream()
                    .flatMap(project -> roleBindingStrategy.authForProject(project, namingStrategy).stream())
                    .collect(Collectors.toSet());
        } else {
            acls = domain.projects.stream()
                    .flatMap(project -> aclStrategy.authForProject(project, namingStrategy).stream())
                    .collect(Collectors.toSet());
        }
        // get all fully qualified application IDs
        final String streamsInternalTopicSeparator = "-";

        // we add the separator (which is actually an implementation detail) to ensure
        // that removal of one streams app does not imply removal of internal topics
        // of another streams app whose name extends the first app's name.
        // otherwise we would have to ensure that no application id is another
        // application id's prefix
        final var streamsInternalTopicPrefixes = domain.projects.stream()
                .flatMap(project -> project.streamsApps.stream())
                .map(namingStrategy::name)
                .map(s -> s + streamsInternalTopicSeparator)
                .collect(Collectors.toSet());

        return new ClusterState(acls, bindings, topics, streamsInternalTopicPrefixes);
    }
}
