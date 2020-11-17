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

    private final static Logger logger = LoggerFactory.getLogger(MapTools.class);

    /**
     * Applies a function to each value of a map.
     *
     * @param map the input map
     * @param valueMapper function to apply to each value of the map
     * @param <K> type of map keys
     * @param <V1> input type of the function
     * @param <V2> output type of the function
     * @return a new Map whose values have been computed from the input map
     */
    public static <K,V1, V2> Map<K, V2> mapValues(Map<K, V1> map, Function<V1, V2> valueMapper) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueMapper.apply(e.getValue())));
    }

    /**
     * Applies a function to each value of a map and drops those entries for which null is returned.
     *
     * @param map the input map
     * @param valueMapper function to apply to each value of the map
     * @param <K> type of map keys
     * @param <V1> input type of the function
     * @param <V2> output type of the function
     * @return a new Map whose values have been computed from the input map
     */
    public static <K,V1, V2> Map<K, V2> mapValuesDropNull(Map<K, V1> map, Function<V1, V2> valueMapper) {
        final Map<K, V2> result = new HashMap<>();
        map.forEach((k, v) -> {
            final var res = valueMapper.apply(v);
            if (res != null) {
                result.put(k, res);
            }
        });
        return result;
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

    /**
     * Groups a list by a specific key.
     *
     * Entries for which {@code keyExtractor} returns null are skipped.
     *
     * @param input the input list
     * @param keyExtractor function used to extract key
     * @param <K> type of keys
     * @param <V> type of values
     * @return a map which contains the entries of the list, grouped by
     */
    public static <K,V> Map<K, List<V>> groupBy(List<V> input, Function<V, K> keyExtractor) {
        final Map<K, List<V>> grouped = new HashMap<>();
        input.forEach(v -> {
            final K key = keyExtractor.apply(v);
            if (key != null) {
                grouped.computeIfAbsent(key, k -> new ArrayList<>());
                grouped.get(key).add(v);
            }
        });
        return grouped;
    }

    /**
     * Constructs a map from a list: each entry in the list will be split into a key/value pair using the provided
     * splitter function.
     *
     * Entries for which the splitter function returns a list whose size does not equal 2 will not be included in the map.
     *
     * @param input the input list
     * @param splitter function used to construct key/value pair from entry of list
     * @param <T> type of keys and values
     * @return a map with entries for each line
     */
    public static <T> Map<T, T> mapFromList(List<T> input, Function<T, List<T>> splitter) {
        final Map<T, T> result = new HashMap<>();

        for (T t : input) {
            final var list = splitter.apply(t);
            if (list != null && list.size() == 2) {
                result.put(list.get(0), list.get(1));
            } else {
                logger.error("entry {} cannot be split into two parts, skipping it", t);
            }
        }
        return result;
    }
}
