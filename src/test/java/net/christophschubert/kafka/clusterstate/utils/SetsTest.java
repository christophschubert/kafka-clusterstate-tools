package net.christophschubert.kafka.clusterstate.utils;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class SetsTest {

    final Set<String> ab = Set.of("a", "b");
    final Set<String> cd = Set.of("c", "d");
    final Set<String> ac = Set.of("a", "c");

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

    @Test
    public void union() {
        final var union = Sets.union(ab, cd);
        assertEquals(Set.of("a", "b", "c", "d"), union);
    }

    @Test
    public void intersection() {
        assertEquals(Collections.emptySet(), Sets.intersection(ab, cd));
        assertEquals(Set.of("a"), Sets.intersection(ab, ac));
    }

    @Test
    public void setMinus() {
        assertEquals(Set.of("c"), Sets.setMinus(ac, ab));
    }

}