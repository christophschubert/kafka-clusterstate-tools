package net.christophschubert.kafka.clusterstate.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.christophschubert.kafka.clusterstate.formats.env.CloudCluster;
import net.christophschubert.kafka.clusterstate.formats.env.Environment;
import net.christophschubert.kafka.clusterstate.utils.MapTools;

import org.apache.commons.logging.Log;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;


public class PropertyMergeTool {

    public static Properties getPropsFrom_ENV_VARS_AND_DESCRIPTOR(String envFilePath, String defautl_cluster_name) {

        // read target cluster name from ENV_VARS
        String targetClusterName = EnvVarTools.readPropertyFromEnv( "KST" , "TARGET_CLUSTER_NAME" );

        if ( targetClusterName != null )
            return PropertyMergeTool.getClientProperties(envFilePath, targetClusterName);
        else
            return PropertyMergeTool.getClientProperties( envFilePath, defautl_cluster_name );

    }

    public static Properties getClientProperties(String environmentPath, String clusterName ) {
        System.out.println( "> merge API keys from ENV_VARS with cluster client property template for cluster: [" + clusterName + "] from ENV-DESCRIPTOR: " + environmentPath );
        return getClientProperties( new File(environmentPath), clusterName );
    }


    public static Properties getClientProperties(File environmentPath, String clusterName ) {
        CloudCluster cl = getCloudCluster( environmentPath, clusterName );
        return getClientProperties( cl, clusterName );
    }

        /**
         * create a properties object from an environment (domain files can be extracted from environment)
         */
    public static Properties getClientProperties(CloudCluster cluster, String clusterName ) {

        try {

                Map<String, String> configMap = CLITools.getClientProps(cluster);

                final var substitutions = EnvVarTools.extractEnvVarsForCluster(clusterName, System.getenv());

                final StringSubstitutor substitutor = new StringSubstitutor(substitutions);

                configMap.putAll(MapTools.mapValues(CLITools.getClientProps(cluster), substitutor::replace));

                Properties properties = new Properties();

                properties.putAll(configMap);

                return properties;

        }
        catch (Exception e) {
            //TODO: proper error handling
            e.printStackTrace();
            return null;
        }

    }

    /**
     * create a properties object from an environment (domain files can be extracted from environment)
     */
    public static CloudCluster getCloudCluster(File environmentPath, String clusterName ) {

        try {

            final var objectMapper = new ObjectMapper(new YAMLFactory());

            final var environment = objectMapper.readValue(environmentPath, Environment.class);
            final var maybeCluster = getCluster(environment, clusterName);
            if (maybeCluster.isEmpty()) {
                System.err.println("Could not load cluster " + clusterName);
                System.exit(1);
            }
            else {
                return maybeCluster.get();
            }
        }
        catch (Exception e) {
                //TODO: proper error handling
                e.printStackTrace();
                return null;
        }

        return null;

    }

    static Optional<CloudCluster> getCluster(Environment environment, String clusterName) {
        return environment.clusters.stream().filter(cluster -> cluster.name.equals(clusterName)).findFirst();
    }


}
