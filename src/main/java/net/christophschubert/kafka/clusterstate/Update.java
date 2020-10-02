package net.christophschubert.kafka.clusterstate;

import java.util.Objects;
//TODO: remove this class and replace with before/after pair
public class Update<T> {
    public final T before;
    public final T after;

    public Update(T before, T after) {
        this.before = before;
        this.after = after;
    }

    public static <T> Update<T> of(T before, T after) {
        return new Update(before, after);
    }

    @Override
    public String toString() {
        return "<" + before + ", " + after + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Update)) return false;
        Update<?> pair = (Update<?>) o;
        return Objects.equals(before, pair.before) &&
                Objects.equals(after, pair.after);
    }

    @Override
    public int hashCode() {
        return Objects.hash(before, after);
    }
}
