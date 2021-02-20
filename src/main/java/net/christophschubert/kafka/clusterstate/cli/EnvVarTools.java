package net.christophschubert.kafka.clusterstate.cli;

import java.util.*;

public class EnvVarTools {

    /**
     * Converts a environment variable name to a corresponding property name.
     *
     * <p>Conversion happens according to the following rules:
     *
     * <p>- drop prefix and a single separator character - convert to lower-case - replace every '_'
     * with a '.', can be escaped by using "___", which will be replaced by a single '_'
     *
     * @param envVarName
     * @param prefixToDrop
     * @return
     */
    static String envVarNameToPropertyName(String envVarName, String prefixToDrop) {
        final int dropLength = prefixToDrop.length() + 1;
        return envVarName.substring(dropLength).toLowerCase().replace('_', '.').replace("...", "_");
    }

    /**
     * The clustername in is <clusterName>, hence the following entries from the envVars-map will be used for substitutions:
     *
     *  KST_CLUSTER_API_SECRET_<clusterName>
     *  KST_CLUSTER_API_KEY_<clusterName>
     *  KST_SR_API_SECRET_<clusterName>
     *  KST_SR_API_KEY_<clusterName>
     *
     * Note: the prefix KST is currently fixed (for Kafka Cluster State Tools).
     *
     * @param clusterName
     * @param envVars
     * @return res
     */
    static Map<String, String> extractEnvVarsForCluster(String clusterName, Map<String, String> envVars) {
        final String kstEnvVarPrefix = "KST";
        return extractEnvVarsForCluster( clusterName, envVars, kstEnvVarPrefix );
    }

    static Map<String, String> extractEnvVarsForCluster(String clusterName, Map<String, String> envVars, String kstEnvVarPrefix ) {
        final var varKeys = List.of("CLUSTER_API_KEY", "CLUSTER_API_SECRET", "SR_API_KEY", "SR_API_SECRET");
        return extractEnvVarsForCluster( clusterName, envVars, kstEnvVarPrefix, varKeys);
    }

    static Map<String, String> extractEnvVarsForCluster(String clusterName, Map<String, String> envVars, String kstEnvVarPrefix, List<String> varKeys) {

        final Map<String, String> res = new HashMap<>();
        varKeys.forEach(key -> {
            final var envVarName = kstEnvVarPrefix + "_" + key + "_" + clusterName;
            res.put(key, envVars.get(envVarName));
        });
        return res;
    }

    public static String readPropertyFromEnv(String kst, String target_cluster_name_property_name) {
        return System.getenv().get( kst + "_" + target_cluster_name_property_name );
    }

}