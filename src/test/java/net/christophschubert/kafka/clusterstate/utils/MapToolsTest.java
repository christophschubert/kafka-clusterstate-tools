package net.christophschubert.kafka.clusterstate.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MapToolsTest {

    @Test
    public void filterKeysMapValuesTest() {
        final var input = Map.of("keep1", "v1", "drop", "dropMe", "keep2", "v2");
        final var result = MapTools.filterKeysMapValues(input, s -> s.startsWith("keep"), String::toUpperCase);
        assertEquals(Map.of("keep1", "V1", "keep2", "V2"), result);
    }

    @Test
    public void mapFromListTest() {
        final var list = List.of("ab:tv", "ac:b", "abcd", "x:y:z", "ac:new");
        assertEquals(
                Map.of("ab", "tv", "ac", "new"),
                MapTools.mapFromList(list, s -> Arrays.asList(s.split(Pattern.quote(":"))))
        );
    }

}