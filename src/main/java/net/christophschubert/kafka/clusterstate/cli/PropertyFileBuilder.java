package net.christophschubert.kafka.clusterstate.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.christophschubert.kafka.clusterstate.formats.env.CloudCluster;
import net.christophschubert.kafka.clusterstate.formats.env.Environment;
import picocli.CommandLine;
import org.apache.commons.text.StringSubstitutor;

import java.io.*;
import java.io.PrintStream;
import java.util.*;

/**
 * create a property file from an environment (domain files can be extracted from environment)
 */
@CommandLine.Command(name= "propertygenerator", subcommands = { CommandLine.HelpCommand.class }, version = "0.1.0",
        description = "Generate property files for application.")
public class PropertyFileBuilder {
    @CommandLine.Command(name = "build", description = "Apply domain description from context to a cluster")
    int build (
            @CommandLine.Parameters(paramLabel = "environment", description = "path to env file") File environmentPath,
            @CommandLine.Parameters(paramLabel = "cluster") String clusterName
    ) {

        try {

            final var objectMapper = new ObjectMapper(new YAMLFactory());

            final var substitutions = EnvVarTools.extractEnvVarsForCluster( clusterName, System.getenv() );

            // printProps(substitutions, System.out );

            final StringSubstitutor substitutor = new StringSubstitutor(substitutions);

            final var clientProps = new Properties();

            final var environment = objectMapper.readValue(environmentPath, Environment.class);
            final var maybeCluster = getCluster(environment, clusterName);
            if (maybeCluster.isEmpty()) {
                System.err.println("Could not load cluster " + clusterName);
                System.exit(1);
            }
            else {

                Properties cpp = PropertyMergeTool.getClientProperties( maybeCluster.get() , clusterName );

                /**
                 * Export properties files into cpl folder.
                 */
                PrintStream ps = getPropertyFileWriter(environmentPath, clusterName);

                printProps(cpp, ps);

                ps.flush();
                ps.close();

            }


        } catch (Exception e) {
            //TODO: proper error handling
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    /**
     * We create a PrintStream instance to export the properties to a file in a "cpf" named folder in the same folder
     * as the environment file.
     *
     * @param environmentPath
     * @param clusterName
     * @return
     * @throws Exception
     */
    PrintStream getPropertyFileWriter(File environmentPath, String clusterName) throws Exception {
        File parent = environmentPath.getParentFile();
        File cpf = new File( parent.getAbsolutePath() + "/cpf/cp-client-props-" + clusterName + ".properties" );
        File cpfParent = cpf.getParentFile();
        cpfParent.mkdirs();
        PrintStream fw = new PrintStream( cpf );
        return fw;
    }

    Optional<CloudCluster> getCluster(Environment environment, String clusterName) {
        return environment.clusters.stream().filter(cluster -> cluster.name.equals(clusterName)).findFirst();
    }

    void printProps(Properties props, PrintStream out) {
        props.list( out );
    }
    void printProps(Map<String, String> props, PrintStream out) {
        props.forEach((k, v) -> out.println(k + "=" + v));
    }

    public static void main(String... args) {
        final int status = new CommandLine(new PropertyFileBuilder()).execute(args);
        System.exit(status);
    }

}
