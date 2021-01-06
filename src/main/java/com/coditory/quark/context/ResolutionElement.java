package com.coditory.quark.context;

import java.util.Objects;

class ResolutionElement {
    private final Class<?> type;
    private final String name;

    ResolutionElement(Class<?> type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolutionElement that = (ResolutionElement) o;
        return Objects.equals(type, that.type) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    public String toShortString() {
        String result = type.getSimpleName();
        if (name != null) {
            result += " (" + name + ")";
        }
        return result;
    }

    @Override
    public String toString() {
        return "ResolutionElement{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
