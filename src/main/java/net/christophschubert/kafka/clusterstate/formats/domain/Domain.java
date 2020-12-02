package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class Domain {
    @JsonProperty("name")
    public final String name;

    @JsonProperty("projects")
    public final Set<Project> projects;

    void updateChildren() {
        projects.forEach(project -> {
            project.parent = this;
            //project.updateChildren();
        });
    }

    @JsonCreator
    public Domain(
            @JsonProperty("name") String name,
            @JsonProperty("projects") Set<Project> projects) {
        this.name = name;
        this.projects = projects == null ? Collections.emptySet() : projects;
        updateChildren();
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Domain)) return false;
        Domain domain = (Domain) o;
        return Objects.equals(name, domain.name) &&
                Objects.equals(projects, domain.projects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, projects);
    }

    @Override
    public String toString() {
        return "Domain{" +
                "name='" + name + '\'' +
                ", projects=" + projects +
                '}';
    }


}
