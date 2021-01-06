package com.coditory.quark.context;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

class CacheableBeanCreator<T> implements BeanCreator<T> {
    static List<BeanCreator<?>> cacheable(List<BeanCreator<?>> creators) {
        return creators.stream()
                .map(CacheableBeanCreator::cacheable)
                .collect(toList());
    }

    static <T> BeanCreator<T> cacheable(BeanCreator<T> creator) {
        return creator instanceof CacheableBeanCreator
                ? creator
                : new CacheableBeanCreator<>(creator);
    }

    private final BeanCreator<T> creator;
    private T bean;

    CacheableBeanCreator(BeanCreator<T> creator) {
        this.creator = requireNonNull(creator);
    }

    @Override
    public T create(ResolutionContext context) {
        if (bean == null) {
            bean = creator.create(context);
        }
        return bean;
    }
}
