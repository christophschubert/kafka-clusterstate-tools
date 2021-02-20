package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.formats.domain.Project;
import net.christophschubert.kafka.clusterstate.formats.domain.ProjectSubResource;

/**
 * Used to generate fully qualified names for topics, consumer groups as well
 * we project-wide names for prefixed ACLs and role-bindings.
 * <p>
 * Every implementation must fulfill: name(resource).startsWith(projectPrefix(project)) for
 * each resource which is part of project.
 * <p>
 * This invariant holds with the default implementation of name.
 */
public interface ResourceNamingStrategy {
    default String name(ProjectSubResource resource) {
        return name(resource.parent, resource.id());
    }

    default String name(Project project, String resourceName) {
        return projectPrefix(project) + resourceName;
    }

    String projectPrefix(Project project);
}
