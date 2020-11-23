package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.junit.Test;

import javax.crypto.spec.PSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DomainParserTest {

    @Test
    public void jsonRoundTrip() throws JsonProcessingException {
        final Domain domain = new Domain("dname", Set.of(
                Project.builder("projectA").addTopic("topic1").addTopic("topic2").build(),
                Project.builder("projectB").addTopic("topic3").addTopic("topic4").build()
        ));

        final DomainParser parser = new DomainParser();
        final String serialized = parser.serialize(domain, DomainParser.Format.JSON);

        final Domain afterRoundTrip = parser.deserialize(serialized, DomainParser.Format.JSON, null);
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

        final Domain afterRoundTrip = parser.deserialize(serialized, DomainParser.Format.YAML, null);
        assertEquals(domain, afterRoundTrip);


        //check that parent/child relations have been set up correctly.
        afterRoundTrip.projects.forEach(project -> {
            assertEquals(afterRoundTrip, project.parent);
            project.topics.forEach(topic -> assertEquals(project, topic.parent));
        });
    }

    @Test
    public void additionalFieldsAreSkipped() {
        //TODO: implement
    }

    @Test
    public void schemaFileNamesBecomeAbsolut() throws JsonProcessingException {
        final var raw = "---\n" +
                "name: test\n" +
                "projects:\n" +
                "- name: helloproj\n" +
                "  topics:\n" +
                "  - name: topicB\n" +
                "    dataModel:\n" +
                "      key:\n" +
                "        type: String\n" +
                "      value:\n" +
                "        type: Avro\n" +
                "        schemaFile: \"schemas/test.avsc\"\n";

        final Domain domain = new DomainParser().deserialize(raw, DomainParser.Format.YAML, "basePath");
        assertEquals("basePath/schemas/test.avsc",
                domain.projects.iterator().next().topics.iterator().next().dataModel.value.schemaFile);
    }


    @Test
    public void dddd() throws IOException {
        final DomainParser parser = new DomainParser();
        final var examples = parser.loadFromFile(Path.of("examples", "simple-context", "pro2.domy").toFile());
        System.out.println(examples);
    }

}