package net.christophschubert.kafka.clusterstate.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class MapToolsTest {

    @Test
    public void mapValuesTest() {
        final var input = Map.of("a", "av", "b", "bvv");
        final var result = MapTools.mapValues(input, String::length);
        assertEquals(Map.of("a", 2, "b", 3), result);
    }

    @Test
    public void mapValuesDropNullTest() {
        final var input = Map.of("a", "av", "b", "bvv", "c", "marker");
        final var result = MapTools.mapValuesDropNull(input, s -> s.equals("marker") ? null : s.length());
        assertEquals(Map.of("a", 2, "b", 3), result);
    }

    @Test
    public void filterKeysMapValuesTest() {
        final var input = Map.of("keep1", "v1", "drop", "dropMe", "keep2", "v2");
        final var result = MapTools.filterKeysMapValues(input, s -> s.startsWith("keep"), String::toUpperCase);
        assertEquals(Map.of("keep1", "V1", "keep2", "V2"), result);
    }

    @Test
    public void filterMapKeysTest() {
        final var input = Map.of("keep1", "v1", "drop", "dropMe", "keep2", "v2");
        final var result = MapTools.filterMapKeys(input, s -> s.startsWith("keep"), String::toUpperCase);
        assertEquals(Map.of("KEEP1", "v1", "KEEP2" ,"v2"), result);
    }

    @Test
    public void groupByTest() {
        final var input = List.of("hello", "world", "how", "are", "you", "?");
        final var group = MapTools.groupBy(input, String::length);

        assertEquals(Map.of(
                1, List.of("?"),
                3, List.of("how", "are", "you"),
                5, List.of("hello", "world")),
                group);
    }

    @Test
    public void groupByWillDropNullValuesTest() {
        final var input = List.of("hello", "marker", "world", "hello");
        final var group = MapTools.groupBy(input, s -> s.equals("marker") ? null : s);
        assertEquals(Map.of(
                "hello", List.of("hello", "hello"),
                "world", List.of("world")),
                group);
    }

    @Test
    public void mapFromListTest() {
        // this input should also generate ERROR log output for "x:y:z"
        final var list = List.of("ab:tv", "ac:b", "nonSplit", "x:y:z", "ac:new");
        assertEquals(
                Map.of("ab", "tv", "ac", "new"),
                MapTools.mapFromList(list, s -> Arrays.asList(s.split(Pattern.quote(":"))))
        );
    }

}