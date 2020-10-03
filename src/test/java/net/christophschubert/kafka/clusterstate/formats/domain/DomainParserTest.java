package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DomainParserTest {

    @Test
    public void jsonRoundTrip() throws JsonProcessingException {
        final Domain domain = new Domain("dname", Set.of(
                Project.builder("projectA").addTopic("topic1").addTopic("topic2").build(),
                Project.builder("projectB").addTopic("topic3").addTopic("topic4").build()
        ));

        final DomainParser parser = new DomainParser();
        final String serialized = parser.serialize(domain, DomainParser.Format.JSON);

        final Domain afterRoundTrip = parser.deserialize(serialized, DomainParser.Format.JSON);
        assertEquals(domain, afterRoundTrip);

        //check that parent/child relations have been set up correctly.
        afterRoundTrip.projects.forEach(project -> {
            assertEquals(afterRoundTrip, project.parent);
            project.topics.forEach(topic -> assertEquals(project, topic.parent));
        });
    }

    @Test
    public void yamlRoundTrip() throws JsonProcessingException {
        final Domain domain = new Domain("dname", Set.of(
                Project.builder("projectA").addTopic("topic1").addTopic("topic2").build(),
                Project.builder("projectB").addTopic("topic3").addTopic("topic4").build()
        ));

        final DomainParser parser = new DomainParser();
        final String serialized = parser.serialize(domain, DomainParser.Format.YAML);

        final Domain afterRoundTrip = parser.deserialize(serialized, DomainParser.Format.YAML);
        assertEquals(domain, afterRoundTrip);

        //check that parent/child relations have been set up correctly.
        afterRoundTrip.projects.forEach(project -> {
            assertEquals(afterRoundTrip, project.parent);
            project.topics.forEach(topic -> assertEquals(project, topic.parent));
        });
    }

    @Test
    public void additionalFieldsAreSkipped() {

    }
}