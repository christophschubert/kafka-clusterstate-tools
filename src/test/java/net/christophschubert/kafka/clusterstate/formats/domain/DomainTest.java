package net.christophschubert.kafka.clusterstate.formats.domain;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class DomainTest {

    @Test
    public void constructingDomainShouldSetUpParentRelations() {
        final Domain domain = new Domain("dname", Set.of(
                Project.builder("projectA").addTopic("topic1").addTopic("topic2").build(),
                Project.builder("projectB").addTopic("topic3").addTopic("topic4").build()
        ));

        domain.projects.forEach(project -> {
            assertEquals(domain, project.parent);
            project.topics.forEach(topic -> assertEquals(project, topic.parent));
        });
    }

}