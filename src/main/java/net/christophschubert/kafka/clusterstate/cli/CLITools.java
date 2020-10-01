package net.christophschubert.kafka.clusterstate.cli;

import net.christophschubert.kafka.clusterstate.MapTools;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

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
}
