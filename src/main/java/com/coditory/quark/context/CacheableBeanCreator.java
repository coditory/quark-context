package com.coditory.quark.context;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

final class CacheableBeanCreator<T> implements BeanCreator<T> {
    static List<CacheableBeanCreator<?>> cacheable(List<BeanCreator<?>> creators) {
        return creators.stream()
                .map(CacheableBeanCreator::cacheable)
                .collect(toList());
    }

    static <T> CacheableBeanCreator<T> cacheable(BeanCreator<T> creator) {
        return creator instanceof CacheableBeanCreator
                ? (CacheableBeanCreator<T>) creator
                : new CacheableBeanCreator<>(creator);
    }

    private final BeanCreator<T> creator;
    private T bean;

    CacheableBeanCreator(BeanCreator<T> creator) {
        this.creator = requireNonNull(creator);
    }

    boolean isCached() {
        return bean != null;
    }

    @Override
    public T create(ResolutionContext context) {
        if (bean == null) {
            bean = creator.create(context);
        }
        return bean;
    }
}
