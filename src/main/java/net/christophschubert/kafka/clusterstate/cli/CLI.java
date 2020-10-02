package net.christophschubert.kafka.clusterstate.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.christophschubert.kafka.clusterstate.*;
import net.christophschubert.kafka.clusterstate.actions.Action;
import net.christophschubert.kafka.clusterstate.formats.domain.Domain;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.DomainCompiler;
import net.christophschubert.kafka.clusterstate.formats.domain.DomainParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

        final DomainParser parser = new DomainParser();

        final List<Domain> domains = Files.list(contextPath.toPath())
                .filter(CLITools::isDomainFile)
                .flatMap(path -> {
                    try {
                        return Stream.of(parser.loadFromFile(path.toFile()));
                    } catch (IOException e) {
                        logger.error("Could not open domain file " + path , e);
                    }
                    return Stream.empty();
                }).collect(Collectors.toList());


        final DomainCompiler compiler = new DomainCompiler();
        final var boringStrategy = new DomainCompiler.BoringStrategy();
        final Map<String, List<Domain>> groupedDomains = MapTools.groupBy(domains, Domain::name);
        final Map<String, ClusterState> clusterStateByDomain = MapTools.mapValues(groupedDomains, domainList ->
                domainList.stream()
                        .map(domain -> compiler.compile(domain, boringStrategy, DomainCompiler.es))
                        .reduce(ClusterState.empty, (s1, s2) -> s1.merge(s2)));


        ClientBundle bundle = ClientBundle.fromProperties(properties);
        final ClusterState currentState = ClusterStateManager.build(bundle);

        clusterStateByDomain.forEach((domainName, desiredState) -> {


            final ClusterState clusterDomainState = currentState.filterByPrefix(domainName);

            final ClusterStateDiff stateDiff = new ClusterStateDiff(clusterDomainState, desiredState);

            final ClusterStateManager clusterStateManager = new ClusterStateManager();

            final List<Action> actions = clusterStateManager.buildActionList(stateDiff);
            System.out.println(actions.size());
            actions.forEach(action -> {
                try {
                    action.runRaw(bundle);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });


        //next steps
        //TODO: implement filterByPrefix properly on ClusterState


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
