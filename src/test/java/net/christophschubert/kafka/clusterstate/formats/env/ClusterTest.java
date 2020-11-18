package net.christophschubert.kafka.clusterstate.formats.env;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClusterTest {

    @Test
    public void parserTest() throws JsonProcessingException {
        final String raw = "#########################################################################################\n" +
                "# This file describes three Confluent Platform environments, used for our demo scenario.\n" +
                "#\n" +
                "clusters:\n" +
                "- type: developer-sandbox #dev/QA/prod, defines the 'environment'\n" +
                "  # from cloud ui\n" +
                "  clusterId: lkc-jwgvw\n" +
                "  clusterType: basic\n" +
                "  provider: GCP\n" +
                "  region: us-east1\n" +
                "  availability: single zone\n" +
                "  name: cluster_AO # from, e.g., the Web UI\n" +
                "\n" +
                "  # defined by organization\n" +
                "  owner: Mirko\n" +
                "  ownerContact: mirko@confluent.io\n" +
                "  org: Confluent Inc.\n" +
                "  tags:\n" +
                "    - low-performance\n" +
                "    - insecure\n" +
                "    - open\n" +
                "  clientProperties:\n" +
                "    # technical details for accessing kafka-service\n" +
                "    kafka:\n" +
                "      bootstrap.servers: pkc-4yyd6.us-east1.gcp.confluent.cloud:9092\n" +
                "      security.protocol: SASL_SSL\n" +
                "      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CLUSTER_API_KEY }}'   password='{{ CLUSTER_API_SECRET }}';\n" +
                "      sasl.mechanism: PLAIN\n" +
                "\n" +
                "    # technical details for accessing schema-registry-service\n" +
                "    schemaRegistry:\n" +
                "      schema.registry.url: https://psrc-4v1qj.eu-central-1.aws.confluent.cloud\n" +
                "      basic.auth.credentials.source: USER_INFO\n" +
                "      schema.registry.basic.auth.user.info: \"{{ SR_API_KEY }}:{{ SR_API_SECRET }}\"\n" +
                "\n" +
                "    # technical details to access the kubernetes stack\n" +
                "    knowledgeGraphService:\n" +
                "      type: Neo4J-service\n" +
                "      url: http://localhost:7474\n" +
                "\n" +
                "#\n" +
                "# Here, we have only essential properties, no optional Kubernetes stack and no graph-db.\n" +
                "#\n" +
                "- type: QA-CloudCluster\n" +
                "    # from cloud ui\n" +
                "  clusterId: lkc-jwxyz\n" +
                "  clusterType: basic\n" +
                "  provider: GCP\n" +
                "  region: us-east1\n" +
                "  availability: single zone\n" +
                "  name: cluster_AO\n" +
                "  # defined by organization\n" +
                "  owner: Jon\n" +
                "  ownerContact: jon@ao.com\n" +
                "  org: AO UK\n" +
                "  tags:\n" +
                "    - high-performance\n" +
                "    - secure\n" +
                "    - managed\n" +
                "\n" +
                "  clientProperties:\n" +
                "    # technical details for accessing kafka-service\n" +
                "    kafka:\n" +
                "      bootstrap.servers: pkc-4yyd6.us-east1.gcp.confluent.cloud:9092\n" +
                "      security.protocol: SASL_SSL\n" +
                "      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CLUSTER_API_KEY }}'   password='{{ CLUSTER_API_SECRET }}';\n" +
                "      sasl.mechanism: PLAIN\n" +
                "    # technical details for accessing schema-registry-service\n" +
                "    schemaRegistry:\n" +
                "      schema.registry.url: https://psrc-4v1qj.eu-central-1.aws.confluent.cloud\n" +
                "      basic.auth.credentials.source: USER_INFO\n" +
                "      schema.registry.basic.auth.user.info: \"{{ SR_API_KEY }}:{{ SR_API_SECRET }}\"\n" +
                "\n" +
                "#\n" +
                "# Here, we have only essential properties, no optional Kubernetes stack and no graph-db.\n" +
                "#\n" +
                "- type: PROD-CloudCluster\n" +
                "  # from cloud ui\n" +
                "  clusterId: lkc-jwxyz\n" +
                "  clusterType: basic\n" +
                "  provider: GCP\n" +
                "  region: us-east1\n" +
                "  availability: single zone\n" +
                "  name: AO-PROD-cluster-101\n" +
                "  # defined by organization\n" +
                "  owner: Jon\n" +
                "  ownerContact: jon@ao.com\n" +
                "  org: AO UK\n" +
                "  tags:\n" +
                "    - high-performance\n" +
                "    - secure\n" +
                "    - managed\n" +
                "  clientProperties:\n" +
                "    kafka:\n" +
                "      # technical details for accessing kafka-service\n" +
                "      bootstrap.servers: pkc-4yyd6.us-east1.gcp.confluent.cloud:9092\n" +
                "      security.protocol: SASL_SSL\n" +
                "      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CLUSTER_API_KEY }}'   password='{{ CLUSTER_API_SECRET }}';\n" +
                "      sasl.mechanism: PLAIN\n" +
                "    # technical details for accessing schema-registry-service\n" +
                "    schemaRegistry:\n" +
                "      schema.registry.url: https://psrc-4v1qj.eu-central-1.aws.confluent.cloud\n" +
                "      basic.auth.credentials.source: USER_INFO\n" +
                "      schema.registry.basic.auth.user.info: \"{{ SR_API_KEY }}:{{ SR_API_SECRET }}\"\n";


        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final var environment = mapper.readValue(raw, Environment.class);
        System.out.println(environment);
    }

}