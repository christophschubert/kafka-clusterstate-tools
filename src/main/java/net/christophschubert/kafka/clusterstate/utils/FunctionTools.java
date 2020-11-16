package net.christophschubert.kafka.clusterstate.utils;

import java.util.List;
import java.util.function.Function;

public class FunctionTools {
    /**
     * Applies a list of functions to a value.
     *
     * apply(List.of(f, g), v) should yield g(f(v)).
     *
     * @param transformations list of transformations to apply to value in left-to-right
     * @param value initial value
     * @param <T> type parameter
     * @return result of applying all transformations to the initial value
     */
    public static <T> T apply(List<Function<T, T>> transformations, T value) {
        T v = value;
        for (Function<T, T> transformation : transformations) {
            v = transformation.apply(v);
        }
        return v;
    }
}
