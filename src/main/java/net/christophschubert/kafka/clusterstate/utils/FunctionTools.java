package net.christophschubert.kafka.clusterstate.utils;

import java.util.List;
import java.util.function.Function;

public class FunctionTools {
    public static <T> T apply(List<Function<T, T>> transformations, T value) {
        T v = value;
        for (Function<T, T> transformation : transformations) {
            v = transformation.apply(v);
        }
        return v;
    }
}
