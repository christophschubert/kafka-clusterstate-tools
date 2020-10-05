package net.christophschubert.kafka.clusterstate;


import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class Integration {


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Properties cloudProperties = new Properties();
        cloudProperties.load(new FileInputStream("_christoph-cloud.properties"));

        final ClientBundle bundle = new ClientBundle(KafkaAdminClient.create(cloudProperties), new File("."));

        final ClusterState build = ClusterStateManager.build(bundle);
        System.out.println(build);
    }
}
