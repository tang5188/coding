package com.sample.demo.common;

public class TwoTuple<A, B> {
    public final A First;
    public final B Second;

    public TwoTuple(A a, B b) {
        this.First = a;
        this.Second = b;
    }
}