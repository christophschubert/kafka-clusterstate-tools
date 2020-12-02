package net.christophschubert.kafka.clusterstate.formats.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.christophschubert.kafka.clusterstate.SubjectNameStrategyName;
import org.junit.Test;

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
    public void additionalFieldsAreSkipped() throws JsonProcessingException {
        final var raw = "---\n" +
                "name: test\n" +
                "newMetaDataField: somevalue\n" +
                "projects:";
        final Domain domain = new DomainParser().deserialize(raw, DomainParser.Format.YAML, "basePath");
        assertEquals(new Domain("test", null), domain);
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
                "        schemaFile: schemas/test.avsc";

        final Domain domain = new DomainParser().deserialize(raw, DomainParser.Format.YAML, "basePath");
        assertEquals("basePath/schemas/test.avsc",
                domain.projects.iterator().next().topics.iterator().next().dataModel.value.schemaFile);
    }

    @Test
    public void topicIsDefaultSubjectNameStrategy() throws JsonProcessingException {
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
        final var dataModel = domain.projects.iterator().next().topics.iterator().next().dataModel;

        assertEquals(
                new DataModel(new TypeInformation("String", null, null),
                        new TypeInformation("Avro", "basePath/schemas/test.avsc", SubjectNameStrategyName.TOPIC)
                ), dataModel
        );
    }

    @Test
    public void subjectNameTest () throws JsonProcessingException {
        final String inputWithSchemas = "---\n" +
                "name: test\n" +
                "projects:\n" +
                "- name: helloproj\n" +
                "  topics:\n" +
                "    - name: topicA\n" +
                "      dataModel:\n" +
                "        key:\n" +
                "          type: Protobuf\n" +
                "          schemaFile: schemas/key.proto\n" +
                "          subjectNaming: record\n" +
                "        value:\n" +
                "          type: Avro\n" +
                "          schemaFile: schemas/test.avsc\n" +
                "          subjectNaming: topicrecord";

        DomainParser parser = new DomainParser();
        final var domain = parser.deserialize(inputWithSchemas, DomainParser.Format.YAML, "basePath");
        final var dataModel = domain.projects.iterator().next().topics.iterator().next().dataModel;
        assertEquals(new DataModel(
                new TypeInformation("Protobuf", "basePath/schemas/key.proto", SubjectNameStrategyName.RECORD),
                new TypeInformation("Avro", "basePath/schemas/test.avsc", SubjectNameStrategyName.TOPICRECORD))
                , dataModel);
    }
}