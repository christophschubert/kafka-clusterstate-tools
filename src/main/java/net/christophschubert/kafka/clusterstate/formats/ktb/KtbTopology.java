package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.christophschubert.kafka.clusterstate.ClusterState;

import java.util.List;
import java.util.Map;

public class KtbTopology {
    String context;

    @JsonCreator
    public KtbTopology(@JsonProperty("context") String context, @JsonProperty("projects") List<KtbProject> projects) {
        this.context = context;
        this.projects = projects;
    }

    public String getContext() {
        return context;
    }

    public List<KtbProject> getProjects() {
        return projects;
    }

    List<KtbProject> projects;

    @Override
    public String toString() {
        return "KtbTopology{" +
                "context='" + context + '\'' +
                ", projects=" + projects +
                '}';
    }

    public static void main(String[] args) throws JsonProcessingException {
        final KtbTopic topic1 = new KtbTopic("atopic1", Map.of("a", "b", "c", "d"));
        final KtbTopic topic2 = new KtbTopic("atopic2", Map.of("a", "b", "c", "d"));
        final KtbTopic topic3 = new KtbTopic("atopic1", Map.of("a", "b", "c", "d"));
        final KtbTopic topic4 = new KtbTopic("atopic2", Map.of("a", "b", "c", "d"));

        final KtbProject ktbProject1 = new KtbProject("proejctA", List.of(topic1, topic2));
        final KtbProject ktbProject2 = new KtbProject("projectB", List.of(topic3, topic4));
        final KtbTopology smurfContext = new KtbTopology("smurfContext", List.of(ktbProject1, ktbProject2));

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        final String s = mapper.writer().writeValueAsString(smurfContext);

        System.out.println(s);

        final KtbTopology ktbTopology = mapper.readValue(s, KtbTopology.class);

        System.out.println(ktbTopology);

        KtbTopologyConverter converter = new KtbTopologyConverter();
        final ClusterState compile = converter.compile(ktbTopology, new KtbTopologyConverter.BoringTopicNameStrategy("."), new KtbTopologyConverter.EmptyAclStrategy());
        System.out.println(compile.topicNames());


    }
}
