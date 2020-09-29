package net.christophschubert.kafka.clusterstate;

import java.util.Objects;

public class Pair<T, U> {
    public final T first;
    public final U second;

    public Pair(T t, U u) {
        first = t;
        second = u;
    }

    public static <T,U> Pair<T, U> of(T t, U u) {
        return new Pair(t, u);
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
