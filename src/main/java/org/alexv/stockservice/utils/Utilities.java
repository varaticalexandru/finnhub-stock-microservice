package org.alexv.stockservice.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.stream.Stream;

public class Utilities {

    // zips two streams into a single stream of pairs
    public static <T, U> Stream<Pair<T, U>> zip(Stream<T> first, Stream<U> second) {
        Iterator<T> iterator1 = first.iterator();
        Iterator<U> iterator2 = second.iterator();
        Stream.Builder<Pair<T, U>> stream = Stream.builder();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            stream.add(Pair.of(iterator1.next(), iterator2.next()));
        }
        return stream.build();
    }
}
