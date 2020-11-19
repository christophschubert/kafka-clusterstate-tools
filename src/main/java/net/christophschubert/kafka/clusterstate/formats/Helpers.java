package net.christophschubert.kafka.clusterstate.formats;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Helpers {
    public static <T> Set<T> emptyForNull(Set<T> aSet) {
        return aSet == null ? Collections.emptySet() : aSet;
    }

    public static <K, V> Map<K, V> emptyForNull(Map<K, V> aMap) {
        return aMap == null ? Collections.emptyMap() : aMap;
    }
}
