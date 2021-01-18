package com.coditory.quark.context;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class BeanDescriptor {
    private String name;
    private Class<?> type;

    BeanDescriptor(Class<?> type) {
        this(type, null);
    }

    BeanDescriptor(Class<?> type, String name) {
        this.type = requireNonNull(type);
        this.name = name;
    }

    BeanDescriptor withType(Class<?> type) {
        return new BeanDescriptor(type, name);
    }

    String getName() {
        return name;
    }

    Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BeanDescriptor{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanDescriptor that = (BeanDescriptor) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
