package net.christophschubert.kafka.clusterstate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapTools {

    public static <K,V1, V2> Map<K, V2> mapValues(Map<K, V1> map, Function<V1, V2> valueMapper) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueMapper.apply(e.getValue())));
    }
    /**
     * Filter a map to keep those key/value pairs which satisfy a predicate.
     *
     * @param map
     * @param p predicate on K
     * @param <K> key-type
     * @param <V> value-type
     * @return a new Map which contains precisely those keys from map which satisfy p
     */
    public static <K,V> Map<K, V> filterKeys(Map<K, V> map, Predicate<K> p) {

       return map.entrySet().stream()
               .filter(kvEntry -> p.test(kvEntry.getKey()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K,V1, V2> Map<K, V2> filterKeysMapValues(Map<K, V1> map, Predicate<K> p, Function<V1, V2> valueMapper) {
        return map.entrySet().stream()
                .filter(kvEntry -> p.test(kvEntry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueMapper.apply(e.getValue())));
    }

    public static <K1, K2, V> Map<K2, V> filterMapKeys(Map<K1, V> map, Predicate<K1> p, Function<K1, K2> keyMapper) {
        return map.entrySet().stream()
                .filter(kvEntry -> p.test(kvEntry.getKey()))
                .collect(Collectors.toMap(e -> keyMapper.apply(e.getKey()), Map.Entry::getValue));
    }

    public static <K,V> Map<K, List<V>> groupBy(List<V> input, Function<V, K> keyExtractor) {
        Map<K, List<V>> grouped = new HashMap<>();
        input.forEach(v -> {
            final K key = keyExtractor.apply(v);
            grouped.computeIfAbsent(key, k -> new ArrayList<>());
            grouped.get(key).add(v);
        });
        return grouped;
    }
}
