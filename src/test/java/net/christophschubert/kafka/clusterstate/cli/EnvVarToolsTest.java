package net.christophschubert.kafka.clusterstate.cli;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EnvVarToolsTest {

    @Test
    public void underscoresShouldBeReplaced() {
        assertEquals("uvw.xyz", EnvVarTools.envVarNameToPropertyName("abc_uvw_xyz", "abc"));
    }

    @Test
    public void tripleUnderscoresShouldConvertToSingle() {
        assertEquals("cde_ghi.klm", EnvVarTools.envVarNameToPropertyName("abc_cde___ghi.klm", "abc"));
    }


}
