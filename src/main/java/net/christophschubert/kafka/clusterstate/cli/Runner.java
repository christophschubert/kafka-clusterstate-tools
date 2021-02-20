package net.christophschubert.kafka.clusterstate.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.christophschubert.kafka.clusterstate.ClientBundle;
import net.christophschubert.kafka.clusterstate.ClusterState;
import net.christophschubert.kafka.clusterstate.ClusterStateDiff;
import net.christophschubert.kafka.clusterstate.ClusterStateManager;
import net.christophschubert.kafka.clusterstate.actions.Action;
import net.christophschubert.kafka.clusterstate.formats.cluster.ClusterLevelPrivileges;
import net.christophschubert.kafka.clusterstate.formats.cluster.compiler.DefaultCompiler;
import net.christophschubert.kafka.clusterstate.formats.domain.Domain;
import net.christophschubert.kafka.clusterstate.formats.domain.DomainParser;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.DefaultStrategies;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.DomainCompiler;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.RbacStrategies;
import net.christophschubert.kafka.clusterstate.formats.domain.compiler.ResourceNamingStrategy;
import net.christophschubert.kafka.clusterstate.mds.ClusterRegistry;
import net.christophschubert.kafka.clusterstate.mds.Scope;
import net.christophschubert.kafka.clusterstate.utils.FunctionTools;
import net.christophschubert.kafka.clusterstate.utils.MapTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Runner {
    private Logger logger = LoggerFactory.getLogger(Runner.class);

    private final List<File> domainContextPaths;
    private final File clusterLevelAccessPath;
    private final Properties properties;
    private final List<Function<ClusterState, ClusterState>> stateTransforms;

    private final ClientBundle bundle;
    private final ClusterState currentState;
    private final ClusterStateManager clusterStateManager;

    public Runner(
            List<File> domainContextPaths,
            File clusterLevelAccessPath,
            Properties properties,
            List<Function<ClusterState, ClusterState>> stateTransforms
    ) throws ExecutionException, InterruptedException {
        this.domainContextPaths = domainContextPaths;
        this.clusterLevelAccessPath = clusterLevelAccessPath;
        this.properties = properties;
        this.stateTransforms = stateTransforms;
        bundle = ClientBundle.fromProperties(properties);
        currentState = ClusterStateManager.build(bundle);
        clusterStateManager = new ClusterStateManager();
    }


    void run() throws InterruptedException, IOException {
        if (clusterLevelAccessPath != null && clusterLevelAccessPath.exists()) {
            applyClusterChanges();
        }
        applyDomainChanges();
    }

    void applyDomainChanges() throws IOException, InterruptedException {
        final DomainParser parser = new DomainParser();

        final List<Domain> domains = domainContextPaths.stream()
                .flatMap(file -> {
                    try {
                        return Files.list(file.toPath());
                    } catch (IOException e) {
                        logger.error("Could not open or parse domain file " + file , e);
                        System.exit(1);
                    }
                    return Stream.empty();
                })
                .filter(CLITools::isDomainFile)
                .flatMap(path -> {
                    try {
                        return Stream.of(parser.loadFromFile(path.toFile()));
                    } catch (IOException e) {
                        logger.error("Could not open or parse domain file " + path , e);
                        // we should quit application here since otherwise the topic specified in the unparseable domain
                        // file will be deleted!
                        logger.error("Stopping processing to prevent possible data loss");
                        System.exit(1);
                    }
                    return Stream.empty(); // to keep compiler happy
                }).collect(Collectors.toList());

        logger.info("Domains: " + domains);

        //create naming strategy
        final String separator = properties.getProperty(CliConfigs.RESOURCE_NAME_SEPARATOR, DefaultStrategies.DefaultNamingStrategy.DEFAULT_SEPARATOR);
        final ResourceNamingStrategy namingStrategy = new DefaultStrategies.DefaultNamingStrategy(separator);

        DomainCompiler compiler;
        if (bundle.mdsClient != null) {
            //generate RBAC bindings
            ClusterRegistry cr = new ClusterRegistry(bundle.mdsClient);
            final var kafkaClusterName = cr.getKafkaNameForId(bundle.mdsClient.metadataClusterId()).get();
            compiler = DomainCompiler.createRoleBindings(
                    namingStrategy, RbacStrategies.strategyForScope(
                            //TODO: clarify whether this is the right scope!
                            Scope.forClusterName(kafkaClusterName)));
        } else {
            compiler = DomainCompiler.createAcls(namingStrategy, DefaultStrategies.aclStrategy);
        }

        final var groupedDomains = MapTools.groupBy(domains, Domain::name);
        final Map<String, ClusterState> clusterStateByDomain =
                MapTools.mapValues(groupedDomains, domainList -> domainList.stream()
                        .map(compiler::compile)
                        .map(cs -> FunctionTools.apply(stateTransforms, cs))
                        .reduce(ClusterState.empty, ClusterState::merge)
                );

        logger.info("Desired cluster-state by domain-name:");
        clusterStateByDomain.forEach((domainName, desiredState) -> {
            logger.info("\t" + domainName + ": " + desiredState + "\n");
        });


        clusterStateByDomain.forEach((domainName, desiredState) -> {
            final ClusterState clusterDomainState = currentState.filterByPrefix(domainName);

            final ClusterStateDiff stateDiff = new ClusterStateDiff(clusterDomainState, desiredState);

            final List<Action> actions = clusterStateManager.buildActionList(stateDiff);

            actions.forEach(action -> {
                try {
                    action.runRaw(bundle);
                    logger.info("Ran " + action);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Failed action " + action, e);
                }
            });
        });
    }

    public void applyClusterChanges() throws IOException {
        logger.info("applying " + clusterLevelAccessPath + " to " + properties.get("bootstrap.server"));
        final var clusterLevelState = currentState.filterClusterLevel();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        System.out.println(clusterLevelAccessPath);
        final var clusterLevelPrivileges = mapper.readValue(clusterLevelAccessPath, ClusterLevelPrivileges.class);

        final var compiler = new DefaultCompiler();
        final var desiredClusterState = compiler.compile(clusterLevelPrivileges);
        logger.info("desired cluster state " + desiredClusterState);
        final var clusterStateDiff = new ClusterStateDiff(clusterLevelState, desiredClusterState);
        final var actions = clusterStateManager.buildActionList(clusterStateDiff);
        actions.forEach(action -> {
            try {
                action.runRaw(bundle);
                logger.info("Ran " + action);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Failed action " + action, e);
            }
        });

    }
}
