package net.christophschubert.kafka.clusterstate.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import java.util.stream.Collectors;

public class MapTools {

    private static Logger logger = LoggerFactory.getLogger(MapTools.class);

    public static <K,V1, V2> Map<K, V2> mapValues(Map<K, V1> map, Function<V1, V2> valueMapper) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueMapper.apply(e.getValue())));
    }
    /**
     * Filter a map to keep those key/value pairs whose keys satisfy a predicate.
     *
     * @param map the input map
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

    /**
     * Returns a new map which contains precisely those keys from the input satisfying a predicate and
     * whose corresponding values have been transformed by the valueMapper.
     *
     * @param map input map
     * @param p the predicate to filter keys on
     * @param valueMapper function to apply to values
     * @param <K> type of the keys
     * @param <V1> input value type
     * @param <V2> output value type
     * @return filtered and transformed map
     */
    public static <K,V1, V2> Map<K, V2> filterKeysMapValues(Map<K, V1> map, Predicate<K> p, Function<V1, V2> valueMapper) {
        return map.entrySet().stream()
                .filter(kvEntry -> p.test(kvEntry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueMapper.apply(e.getValue())));
    }

    /**
     * Filters a map to contain those keys satisfying a predicate and transforms the keys with a transformation
     *
     * @param map input map
     * @param p predicate to test keys against
     * @param keyMapper transformer for keys
     * @param <K1> input key type
     * @param <K2> output key type
     * @param <V> value type
     * @return a map restricted to those keys satisfying p and being transformed with keyMapper
     */
    public static <K1, K2, V> Map<K2, V> filterMapKeys(Map<K1, V> map, Predicate<K1> p, Function<K1, K2> keyMapper) {
        return map.entrySet().stream()
                .filter(kvEntry -> p.test(kvEntry.getKey()))
                .collect(Collectors.toMap(e -> keyMapper.apply(e.getKey()), Map.Entry::getValue));
    }

    public static <K,V> Map<K, List<V>> groupBy(List<V> input, Function<V, K> keyExtractor) {
        final Map<K, List<V>> grouped = new HashMap<>();
        input.forEach(v -> {
            final K key = keyExtractor.apply(v);
            grouped.computeIfAbsent(key, k -> new ArrayList<>());
            grouped.get(key).add(v);
        });
        return grouped;
    }

    public static <T> Map<T, T> mapFromList(List<T> input, Function<T, List<T>> splitter) {
        final Map<T, T> result = new HashMap<>();

        for (T t : input) {
            final var list = splitter.apply(t);
            if (list.size() == 2) {
                result.put(list.get(0), list.get(1));
            } else {
                logger.error("entry {} cannot be split into two parts, skipping it", t);
            }
        }
        return result;
    }
}
