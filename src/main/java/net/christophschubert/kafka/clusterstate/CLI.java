package net.christophschubert.kafka.clusterstate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


@Command(name= "kcs", subcommands = { CommandLine.HelpCommand.class }, version = "kcs 0.1.0",
        description = "Manage metadata of a Apache Kafka cluster.")
class CLI {
    private Logger logger = LoggerFactory.getLogger(CLI.class);
    @Command(name = "extract", description = "Extract the current state of the cluster")
    void extract(
            @Option(names = { "-b", "--bootstrap-server" }, paramLabel = "<bootstrap-server>",
                    description = "bootstrap server of the cluster to connect too") String bootstrapServer,
            @Option(names = { "-c", "--command-properties"}, paramLabel = "<command properties>",
                    description = "command config file ") File configFile,
            @Option(names = { "-f", "--file" }, paramLabel = "STATEFILE", description = "filename to store state") File stateFile
    ) throws IOException, ExecutionException, InterruptedException {
        Properties properties = new Properties();
        if (configFile != null) {
            properties.load(new FileReader(configFile));
        }
        if (bootstrapServer != null) {
            properties.put("bootstrap.servers", bootstrapServer);
        }
        logger.info(properties.toString());

        final ClientBundle bundle = ClientBundle.fromProperties(properties);
        final ClusterState clusterState = ClusterStateManager.build(bundle);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writer().writeValue(stateFile, clusterState);
    }


    public static void main(String[] args) {
        final int status = new CommandLine(new CLI()).execute(args);
        System.exit(status);
    }
}
