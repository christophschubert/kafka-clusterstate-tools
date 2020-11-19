package net.christophschubert.kafka.clusterstate.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.christophschubert.kafka.clusterstate.formats.env.CloudCluster;
import net.christophschubert.kafka.clusterstate.formats.env.Environment;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * create a property file from a environment (domain files can be extracted from environment)
 */
@CommandLine.Command(name= "propertygenerator", subcommands = { CommandLine.HelpCommand.class }, version = "0.1.0",
        description = "Generate property files for application.")
public class PropertyFileBuilder {
    @CommandLine.Command(name = "build", description = "Apply domain description from context to a cluster")
    int build (
            @CommandLine.Parameters(paramLabel = "environment", description = "path to the context") File environmentPath,
            @CommandLine.Parameters(paramLabel = "cluster") String clusterName
    ) {
        final var objectMapper = new ObjectMapper(new YAMLFactory());

        try {
            final var environment = objectMapper.readValue(environmentPath, Environment.class);
            final var maybeCluster = getCluster(environment, clusterName);
            if (maybeCluster.isEmpty()) {
                System.err.println("Could not load cluster " + clusterName);
                System.exit(1);
            }
            final var cluster = maybeCluster.get();
            printProps(CLITools.getClientProps(cluster), System.out);

        } catch (IOException e) {
            //TODO: proper error handling
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    Optional<CloudCluster> getCluster(Environment environment, String clusterName) {
        return environment.clusters.stream().filter(cluster -> cluster.name.equals(clusterName)).findFirst();
    }



    void printProps(Map<String, String> props, PrintStream out) {
        props.forEach((k, v) -> out.println(k + "=" + v));
    }

    public static void main(String... args) {
        final int status = new CommandLine(new PropertyFileBuilder()).execute(args);
        System.exit(status);
    }
}
