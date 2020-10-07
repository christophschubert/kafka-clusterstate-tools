package net.christophschubert.kafka.clusterstate;

import org.junit.Test;

import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class SetsTest {

    @Test
    public void filterSinglePredicate() {
        final var stringSet = Set.of("ax", "ay", "bx");

        final var filtered = Sets.filter(stringSet, a -> a.startsWith("a"));
        assertEquals(Set.of("ax", "ay"), filtered);
    }

    @Test
    public void filterSetOfPredicates() {
        final var stringSet = Set.of("ax", "ay", "bx", "c", "dx");
        final Set<Predicate<String>> predicates = Set.of(a -> a.startsWith("a"), b -> b.startsWith("b"));
        final var filtered = Sets.filterAny(stringSet, predicates);
        assertEquals(Set.of("ax", "ay", "bx"), filtered);
    }

}