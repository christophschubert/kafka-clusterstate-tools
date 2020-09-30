package net.christophschubert.kafka.clusterstate.formats.ktb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.christophschubert.kafka.clusterstate.ClusterState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class KtbTopology {
    String context;
    List<KtbProject> projects;

    @JsonCreator
    public KtbTopology(
            @JsonProperty("context") String context,
            @JsonProperty("projects") List<KtbProject> projects
    ) {
        this.context = context;
        this.projects = projects;
    }

    public String getContext() {
        return context;
    }

    public List<KtbProject> getProjects() {
        return projects;
    }



    @Override
    public String toString() {
        return "KtbTopology{" +
                "context='" + context + '\'' +
                ", projects=" + projects +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KtbTopology)) return false;
        KtbTopology that = (KtbTopology) o;
        return Objects.equals(context, that.context) &&
                Objects.equals(projects, that.projects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, projects);
    }

    public static void main(String[] args) throws JsonProcessingException {
        final KtbTopic topic1 = new KtbTopic("atopic1", Map.of("a", "b", "c", "d"));
        final KtbTopic topic2 = new KtbTopic("atopic2", Map.of("a", "b", "c", "d"));
        final KtbTopic topic3 = new KtbTopic("atopic1", Map.of("a", "b", "c", "d"));
        final KtbTopic topic4 = new KtbTopic("atopic2", Map.of("a", "b", "c", "d"));

        final KtbProject ktbProject1 = KtbProject.builder("proejctA").withTopics(List.of(topic1, topic2)).build();
        final KtbProject ktbProject2 = KtbProject.builder("projectB").withTopics(List.of(topic3, topic4)).withConsumer(new KtbConsumer("prin", "grou")).build();
        final KtbTopology smurfContext = new KtbTopology("smurfContext", List.of(ktbProject1, ktbProject2));

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        final String s = mapper.writer().writeValueAsString(smurfContext);

        System.out.println(s);

        final KtbTopology ktbTopology = mapper.readValue(s, KtbTopology.class);
        System.out.println(smurfContext);
        System.out.println(ktbTopology);

        System.out.println(ktbTopology.equals(smurfContext));

        KtbTopologyConverter converter = new KtbTopologyConverter();
        final ClusterState compile = converter.compile(ktbTopology, new KtbTopologyConverter.BoringTopicNameStrategy("."), KtbTopologyConverter.usePrefixedTopicsAcls);
        System.out.println(compile.topicNames());
       


    }
}
