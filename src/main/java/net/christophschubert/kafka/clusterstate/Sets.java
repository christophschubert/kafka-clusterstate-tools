package net.christophschubert.kafka.clusterstate;

import java.util.HashSet;
import java.util.Set;

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
}
