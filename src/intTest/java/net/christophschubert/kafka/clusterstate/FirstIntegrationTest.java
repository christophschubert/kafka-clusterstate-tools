package net.christophschubert.kafka.clusterstate;


import com.fasterxml.jackson.core.JsonProcessingException;
import net.christophschubert.kafka.clusterstate.actions.Action;
import net.christophschubert.kafka.clusterstate.formats.domain.DomainParser;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.DefaultStrategies;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.DomainCompiler;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.Test;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class FirstIntegrationTest {

    Properties buildProperties(DockerComposeContainer env, String serviceName, int port) {
        final var host = env.getServiceHost(serviceName, port) + ":" + env.getServicePort(serviceName, port);
        final var properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        properties.put("sasl.mechanism" , "PLAIN");
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=admin password=\"admin-secret\";");

        return properties;
    }

    @Test
    public void Test() throws InterruptedException, ExecutionException, JsonProcessingException {


        final DockerComposeContainer environment =
                new DockerComposeContainer<>(new File("src/intTest/resources/envs/simple.yaml"))
                        .withExposedService("kafka_1", 9093, Wait.forListeningPort());
        environment.start();

        final var kafka_1 = environment.getContainerByServiceName("kafka_1").get();
        ContainerState state = (ContainerState)kafka_1;


        ClientBundle bundle = ClientBundle.fromProperties(buildProperties(environment, "kafka_1", 9093));

        final var clusterState = ClusterStateManager.build(bundle);

        final String input = "---\n" +
                "name: test\n" +
                "projects:\n" +
                "- name: configTestProject\n" +
                "  topics:\n" +
                "    - name: topicA\n" +
                "      configs:\n" +
                "        \"num.partitions\": \"12\"\n" +
                "        \"cleanup.policy\": \"compact\"\n" +
                "        \"segment.ms\": \"12000\"\n" +
                "    - name: topicB\n" +
                "      dataModel:\n" +
                "        key:\n" +
                "          type: String\n" +
                "        value:\n" +
                "          type: Integer\n" +
                "  consumers:\n" +
                "    - principal: \"User:xxx\"\n" +
                "      groupId: \"groupForApp\"\n" +
                "      prefixGroup: true";

        DomainParser parser = new DomainParser();
        final var domain = parser.deserialize(input, DomainParser.Format.YAML, null);
        final var compiler = DomainCompiler.createAcls(new DefaultStrategies.DefaultNamingStrategy(), DefaultStrategies.aclStrategy);
        final var clusterState1 = compiler.compile(domain);
        final var clusterStateDiff = new ClusterStateDiff(clusterState, clusterState1);
        final var clusterStateManager = new ClusterStateManager();
        final var actions = clusterStateManager.buildActionList(clusterStateDiff);
        for (Action action : actions) {
            action.runRaw(bundle);
        }

        final var finalClusterState = ClusterStateManager.build(bundle, false);
        assertEquals(3, finalClusterState.aclEntries.size());
        assertEquals(Set.of("test_configTestProject_topicA", "test_configTestProject_topicB"), finalClusterState.topicNames());
        System.out.println(finalClusterState.topicDescriptions.get("test_configTestProject_topicA"));

    }



}
