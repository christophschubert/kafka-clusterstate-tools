package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.*;
import net.christophschubert.kafka.clusterstate.formats.domain.*;
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


    interface ProjectAuthorizationStrategy<A> {
        Set<A> authForProject(Project project, ResourceNamingStrategy namingStrategy);
    }


    private final ResourceNamingStrategy namingStrategy;
    private final ProjectAuthorizationStrategy<RbacBindingInScope> roleBindingStrategy;
    private final ProjectAuthorizationStrategy<ACLEntry> aclStrategy;

    //TODO: document properly
    private SerializationInfo ti2si(TypeInformation ti) {
        if (ti == null) return null;
        return new SerializationInfo(ti.type, ti.schemaFile, ti.subjectNameStrategy);
    }
    //TODO: document properly
    private TopicDataModel convertDataModel(DataModel dm) {
        if (dm == null) {
            return new TopicDataModel(null, null);
        }
        return new TopicDataModel(ti2si(dm.key), ti2si(dm.value));
    }

    private <A> Set<A> collectProjects(Domain domain, ProjectAuthorizationStrategy<A> pas, ResourceNamingStrategy rns) {
        if (pas == null) {
            return Collections.emptySet();
        }
        return domain.projects.stream()
                .flatMap(project -> pas.authForProject(project, rns).stream())
                .collect(Collectors.toSet());
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

        final Set<ACLEntry> acls = collectProjects(domain, aclStrategy, namingStrategy);
        final Set<RbacBindingInScope> bindings = collectProjects(domain, roleBindingStrategy, namingStrategy);

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
