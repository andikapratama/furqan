package com.pratamalabs.furqan.services;

/**
 * Created by andikapratama on 09/12/15.
 */
public class ValueHolder<T> {
    public T value;

    public ValueHolder(T value) {
        this.value = value;
    }

    public static <T> ValueHolder<T> from(T value) {
        return new ValueHolder<>(value);
    }
}
