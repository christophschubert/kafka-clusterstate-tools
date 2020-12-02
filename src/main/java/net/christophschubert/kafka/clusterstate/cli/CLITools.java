package net.christophschubert.kafka.clusterstate.cli;

import net.christophschubert.kafka.clusterstate.formats.env.CloudCluster;
import net.christophschubert.kafka.clusterstate.utils.MapTools;
import net.christophschubert.kafka.clusterstate.formats.domain.DomainParser;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class CLITools {
    static Logger logger = LoggerFactory.getLogger(CLITools.class);

    public static Properties loadProperties(File configFile, String bootstrapServer) {
        return loadProperties(configFile, bootstrapServer, null);
    }

    public static Properties loadProperties(File configFile, String bootstrapServer, String envVarPrefix) {

        Properties properties = new Properties();
        if (configFile != null) {
            try {
                properties.load(new FileReader(configFile));
            } catch (IOException e) {
                logger.error("Could not load properties from file: " + configFile.getAbsolutePath(), e);
            }
        }
        if (bootstrapServer != null) {
            properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        }

        if (envVarPrefix != null) {
            final Map<String, String> mappedEnv = MapTools.filterMapKeys(System.getenv(),
                    varName -> varName.startsWith(envVarPrefix),
                    varName -> EnvVarTools.envVarNameToPropertyName(varName, envVarPrefix));
            properties.putAll(mappedEnv);
        }

        return properties;
    }

    public static Optional<DomainParser.Format> formatFromFileEnding(Path p) {
        if (p.toString().endsWith(".domj"))
            return Optional.of(DomainParser.Format.JSON);
        if (p.toString().endsWith(".domy"))
            return Optional.of(DomainParser.Format.YAML);
        return Optional.empty();
    }

    public static boolean isDomainFile(Path path) {
        final String s = path.toString();
        return s.endsWith(".domj") || s.endsWith(".domy");
    }

    /**
     * Splits a string of the form "k1,v1;k2,v2;k3,v3" into a map
     * {k1: v1, k2: v2, k3:v3}
     * @param input
     * @return
     */
    static Map<String, String> parsePrincipalMapping(String input) {
        final List<String> parts = Arrays.asList(input.split(Pattern.quote(";")));
        return MapTools.mapFromList(parts, s -> Arrays.asList(s.split(Pattern.quote(","))));
    }

    static Map<String, String> getClientProps(CloudCluster cluster) {
        final var props = new HashMap<String, String>();
        props.putAll(cluster.clientProperties.getOrDefault("kafka", Collections.emptyMap()));
        props.putAll(cluster.clientProperties.getOrDefault("schemaRegistry", Collections.emptyMap()));
        return props;
    }


}
