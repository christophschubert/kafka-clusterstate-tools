package net.christophschubert.kafka.clusterstate.formats.domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProjectTest {
    @Test
    public void builderTest() {
        final var freaky = Project.builder().addProducer("User:abc").addTopic("freaky").build();
        System.out.println(freaky);
        System.out.println(freaky.producers.iterator().next().parent);
    }

}