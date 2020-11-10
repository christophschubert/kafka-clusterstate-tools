package net.christophschubert.kafka.clusterstate.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Sets {
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        final Set<T> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        final Set<T> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }

    /**
     * Calculates a \ b = { x in a | x not in b}
     *
     * @param a
     * @param b
     * @param <T>
     * @return
     */
    public static <T> Set<T> setMinus(Set<T> a, Set<T> b) {
        final Set<T> result = new HashSet<>(a);
        result.removeAll(b);
        return result;
    }

    /**
     * Filters a set given a predicate.
     *
     * Returns a new set containing precisely those entries for which the predicate holds.
     *
     * @param entries the set to be filtered
     * @param predicate the predicate to test against
     * @param <T> type of elements of the sets
     * @return a new set containing those elements of entries for which predicate holds
     */
    public static <T> Set<T> filter(Set<T> entries, Predicate<T> predicate) {
        return filterAny(entries, Collections.singleton(predicate));
    }

    /**
     * Filters a set to those elements for which any one of the predicates holds.
     *
     *
     * @param entries the set of entries to filter
     * @param predicates the set of predicates to filter against
     * @param <T> type of elements of the sets
     * @return a new set which contains precisely those elements of entries for which any of the predicates holds.
     */
    public static <T> Set<T> filterAny(Set<T> entries, Set<Predicate<T>> predicates) {
        return entries.stream()
                .filter(entry -> predicates.stream().anyMatch(p -> p.test(entry)))
                .collect(Collectors.toSet());
    }
}
