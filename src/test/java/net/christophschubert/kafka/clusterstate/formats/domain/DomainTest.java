package net.christophschubert.kafka.clusterstate.formats.domain;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class DomainTest {

    @Test
    public void constructingDomainShouldSetUpParentRelations() {

        final Domain domain = new Domain("dname", Set.of(
                new Project("projectA", Set.of(new Topic("topic1"), new Topic("topic2"))),
                new Project("projectB", Set.of(new Topic("topic3"), new Topic("topic4")))
        ));

        domain.projects.forEach(project -> {
            assertEquals(domain, project.parent);
            project.topics.forEach(topic -> assertEquals(project, topic.parent));
        });
    }

}