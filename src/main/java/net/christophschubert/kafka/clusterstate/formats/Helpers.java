package net.christophschubert.kafka.clusterstate.formats;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Helpers {
    public static <T> Set<T> emptyForNull(Set<T> aSet) {
        return aSet == null ? Collections.emptySet() : aSet;
    }
}
