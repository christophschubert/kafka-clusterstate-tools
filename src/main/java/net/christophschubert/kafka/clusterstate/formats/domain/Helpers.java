package net.christophschubert.kafka.clusterstate.formats.domain;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Helpers {
    static <T> Set<T> emptyForNull(Set<T> aSet) {
        return aSet == null ? Collections.EMPTY_SET : aSet;
    }
}
