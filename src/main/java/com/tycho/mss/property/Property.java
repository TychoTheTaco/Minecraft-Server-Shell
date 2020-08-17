package com.tycho.mss.property;

public abstract class Property<T> {

    private final String key;

    private T value;

    public Property(final String key, final T value) {
        this.key = key;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public boolean isValidValue(final T value) {
        return true;
    }
}
