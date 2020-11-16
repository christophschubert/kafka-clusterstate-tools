package net.christophschubert.kafka.clusterstate.utils;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FunctionToolsTest {
    @Test
    public void applyEmptyListIsIdentity() {
        final var v = "test";
        assertEquals(v, FunctionTools.apply(Collections.emptyList(), v));
    }

    @Test
    public void twoFunctionsAppliedInListOrder() {
        final var v = "dropKeep";
        assertEquals("addKeep",
                FunctionTools.apply(List.of(
                  s -> s.substring(4),
                  s -> "add" + s
                ), v)
        );
    }
}