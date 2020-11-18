package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class ProjectSubResource {
    @JsonIgnore
    public Project parent;
    public abstract String id();
}
