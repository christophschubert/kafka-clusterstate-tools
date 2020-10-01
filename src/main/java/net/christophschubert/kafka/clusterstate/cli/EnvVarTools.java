package net.christophschubert.kafka.clusterstate.cli;

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
}