package net.christophschubert.kafka.clusterstate.formats.domain;


public abstract class ProjectSubResource {
    public Project parent;
    public abstract String id();
}
