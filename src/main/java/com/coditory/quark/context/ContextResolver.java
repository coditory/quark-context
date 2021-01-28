package com.coditory.quark.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

final class ContextResolver {
    private ContextResolver() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static Map<BeanDescriptor<?>, List<BeanHolder<?>>> resolve(Set<BeanHolder<?>> beanHolders, Map<String, Object> properties) {
        Set<BeanHolder<?>> holders = new HashSet<>(beanHolders);
        ConditionContext contextWithAllBeans = ConditionContext.from(holders, properties);
        ConditionContext contextWithNoBeans = ConditionContext.from(properties);
        Set<BeanHolder<?>> stableBeanHolders = findStableBeans(holders, contextWithAllBeans, contextWithNoBeans);
        holders.removeAll(stableBeanHolders);
        Set<BeanHolder<?>> additiveBeanHolders = findAdditiveBeans(holders, contextWithAllBeans);
        holders.removeAll(additiveBeanHolders);
        return resolve(stableBeanHolders, additiveBeanHolders, holders, properties);
    }

    private static Set<BeanHolder<?>> findStableBeans(Set<BeanHolder<?>> holders, ConditionContext contextWithAllBeans, ConditionContext contextWithNoBeans) {
        return holders.stream()
                .filter(it -> it.isActive(contextWithNoBeans))
                .filter(it -> it.isActive(contextWithAllBeans))
                .collect(toSet());
    }

    private static Set<BeanHolder<?>> findAdditiveBeans(Set<BeanHolder<?>> holders, ConditionContext contextWithAllBeans) {
        return holders.stream()
                .filter(it -> it.isActive(contextWithAllBeans))
                .collect(toSet());
    }

    private static Map<BeanDescriptor<?>, List<BeanHolder<?>>> resolve(
            Set<BeanHolder<?>> stableBeanHolders,
            Set<BeanHolder<?>> additiveBeanHolders,
            Set<BeanHolder<?>> remainingBeanHolders,
            Map<String, Object> properties
    ) {
        Set<String> beanNames = new HashSet<>();
        Set<BeanHolder<?>> registeredBeans = new HashSet<>();
        Map<BeanDescriptor<?>, List<BeanHolder<?>>> result = new HashMap<>();
        stableBeanHolders.forEach(holder -> {
            addBeanName(beanNames, holder);
            addBeanHolderWithClassHierarchy(result, holder);
            registeredBeans.add(holder);
        });
        ConditionContext context = ConditionContext.from(registeredBeans, properties);
        int registered;
        do {
            do {
                registered = registeredBeans.size();
                for (BeanHolder<?> holder : additiveBeanHolders) {
                    if (canBeAddedToContext(holder, registeredBeans, context)) {
                        addBeanName(beanNames, holder);
                        addBeanHolderWithClassHierarchy(result, holder);
                        registeredBeans.add(holder);
                        context = context.with(holder);
                    }
                }
                additiveBeanHolders.removeAll(registeredBeans);
            } while (registered < registeredBeans.size());
            for (BeanHolder<?> holder : remainingBeanHolders) {
                if (canBeAddedToContext(holder, registeredBeans, context)) {
                    addBeanName(beanNames, holder);
                    addBeanHolderWithClassHierarchy(result, holder);
                    registeredBeans.add(holder);
                    context = context.with(holder);
                    break;
                }
            }
            remainingBeanHolders.removeAll(registeredBeans);
        } while (registered < registeredBeans.size());
        return result;
    }

    private static boolean canBeAddedToContext(BeanHolder<?> holder, Set<BeanHolder<?>> registeredBeans, ConditionContext context) {
        if (!holder.isActive(context)) {
            return false;
        }
        ConditionContext contextWithBean = context.with(holder);
        return registeredBeans.stream()
                .allMatch(registered -> registered.isActive(contextWithBean));
    }

    private static void addBeanName(Set<String> beanNames, BeanHolder<?> holder) {
        String name = holder.getBeanName();
        if (name == null) {
            return;
        }
        if (beanNames.contains(name)) {
            throw new ContextException("Duplicated bean name: " + name);
        }
        beanNames.add(name);
    }

    private static <T> void addBeanHolderWithClassHierarchy(Map<BeanDescriptor<?>, List<BeanHolder<?>>> holders, BeanHolder<T> holder) {
        holder.getBeanClassHierarchyDescriptors().forEach(descriptor -> {
            List<BeanHolder<?>> registeredBeanCreators = holders
                    .computeIfAbsent(descriptor, (k) -> new ArrayList<>());
            registeredBeanCreators.add(holder);
        });
    }
}
