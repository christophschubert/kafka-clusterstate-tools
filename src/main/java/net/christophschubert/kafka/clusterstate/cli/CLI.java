package net.christophschubert.kafka.clusterstate.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.ClusterStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;


@Command(name= "kcs", subcommands = { CommandLine.HelpCommand.class }, version = "kcs 0.1.0",
        description = "Manage metadata of a Apache Kafka cluster.")
class CLI {
    private Logger logger = LoggerFactory.getLogger(CLI.class);

    @Command(name = "apply", description = "Apply domain description from context to a cluster")
    int apply(
            @Option(names = { "-b", "--bootstrap-server" }, paramLabel = "<bootstrap-server>",
                    description = "bootstrap server of the cluster to connect too") String bootstrapServer,
            @Option(names = { "-c", "--command-properties"}, paramLabel = "<command properties>",
                    description = "command config file") File configFile,
            @Option(names = { "-e", "--env-var-prefix"}, paramLabel = "<prefix>",
                    description = "prefix for env vars to be added to properties ") String envVarPrefix,
            @CommandLine.Parameters(paramLabel = "context", description = "path to the context", defaultValue = ".") File contextPath
    ) throws IOException, ExecutionException, InterruptedException {
        if (!contextPath.isDirectory()) {
            logger.error("Given context {} is not a folder", contextPath);
            return 1;
        }
        if (configFile == null) {
            configFile = new File(contextPath, "kst.properties");
        }
        final var properties = CLITools.loadProperties(configFile, bootstrapServer, envVarPrefix);

        //perform additional transformations
        final List<Function<ClusterState, ClusterState>> stateTransforms = new ArrayList<>();
        final String principalMappingEnvVarName = "KST_PRINCIPAL_MAPPING";
        if (System.getenv(principalMappingEnvVarName) != null) {
            final var principalMap = CLITools.parsePrincipalMapping(System.getenv(principalMappingEnvVarName));
            stateTransforms.add(cs -> cs.mapPrincipals(principalMap));
        }

        final Runner runner = new Runner(List.of(contextPath), new File(contextPath, "cluster.yaml"), properties, stateTransforms);
        runner.run();

        return 0;
    }


    @Command(name = "extract", description = "Extract the current state of the cluster")
    void extract(
            @Option(names = { "-b", "--bootstrap-server" }, paramLabel = "<bootstrap-server>",
                    description = "bootstrap server of the cluster to connect too") String bootstrapServer,
            @Option(names = { "-c", "--command-properties"}, paramLabel = "<command properties>",
                    description = "command config file") File configFile,
            @Option(names = { "-e", "--env-var-prefix"}, paramLabel = "<prefix>",
                    description = "prefix for env vars to be added to properties ") String envVarPrefix,
            @Option(names = { "-f", "--file" }, paramLabel = "STATEFILE", description = "filename to store state") File stateFile
    ) throws IOException, ExecutionException, InterruptedException {
        Properties properties = CLITools.loadProperties(configFile, bootstrapServer, envVarPrefix);

        //TODO:
        // remove for production usage: may contain secrets
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
