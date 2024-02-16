package org.alexv.stockservice.mapper;

import java.util.List;

public interface Mapper<A, B> {
    B mapTo(A a);
    A mapFrom(B b);

    List<B> mapTo(List<A> a);
    List<A> mapFrom(List<B> b);
}
