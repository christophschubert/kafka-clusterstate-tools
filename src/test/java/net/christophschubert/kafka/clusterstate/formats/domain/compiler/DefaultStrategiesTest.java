package net.christophschubert.kafka.clusterstate.formats.domain.compiler;

import net.christophschubert.kafka.clusterstate.formats.domain.Producer;
import net.christophschubert.kafka.clusterstate.formats.domain.Project;
import org.junit.Test;


import static org.junit.Assert.*;

public class DefaultStrategiesTest {
    Project p = Project.builder("projectA")
            .addProducer(
                new Producer("User:abc", "trans-id", false, null))
            .addTopic("mytopic").build();
    @Test
    public void pppp() {
        System.out.println(p);
        final var producer = p.producers.iterator().next();

        final var aclStrategy = new DefaultStrategies.DefaultProducerAclStrategy();
        aclStrategy.acls(producer, DefaultStrategies.namingStrategy).forEach(System.out::println);
    }

}