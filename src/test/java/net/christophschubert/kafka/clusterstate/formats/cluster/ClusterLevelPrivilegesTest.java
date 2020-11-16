package net.christophschubert.kafka.clusterstate.formats.cluster;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class ClusterLevelPrivilegesTest {
    @Test
    public void parserTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        final var s = "---\n" +
                "acls:\n" +
                "  idempotentWritePrincipals:\n" +
                "    - User:abc\n" +
                "    - User:xyz";
        final var clusterLevelPrivileges = mapper.readValue(s, ClusterLevelPrivileges.class);
        assertEquals(Set.of("User:abc", "User:xyz" ), clusterLevelPrivileges.acls.idempotentWritePrincipals);
    }
}