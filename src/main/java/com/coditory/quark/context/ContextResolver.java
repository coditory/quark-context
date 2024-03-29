package com.coditory.quark.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

final class ContextResolver {
    private static final Logger log = LoggerFactory.getLogger(ContextResolver.class);

    private ContextResolver() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static Map<BeanDescriptor<?>, List<BeanHolder<?>>> resolve(Set<BeanHolder<?>> beanHolders, Map<String, Object> properties) {
        Set<BeanHolder<?>> holders = new LinkedHashSet<>(beanHolders);
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
                .collect(toCollection(LinkedHashSet::new));
    }

    private static Set<BeanHolder<?>> findAdditiveBeans(Set<BeanHolder<?>> holders, ConditionContext contextWithAllBeans) {
        return holders.stream()
                .filter(it -> it.isActive(contextWithAllBeans))
                .collect(toCollection(LinkedHashSet::new));
    }

    private static Map<BeanDescriptor<?>, List<BeanHolder<?>>> resolve(
            Set<BeanHolder<?>> stableBeanHolders,
            Set<BeanHolder<?>> additiveBeanHolders,
            Set<BeanHolder<?>> remainingBeanHolders,
            Map<String, Object> properties
    ) {
        Set<String> beanNames = new LinkedHashSet<>();
        Set<BeanHolder<?>> registeredBeans = new LinkedHashSet<>();
        Map<BeanDescriptor<?>, List<BeanHolder<?>>> result = new LinkedHashMap<>();
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
        logResolvedBeans(registeredBeans, additiveBeanHolders, remainingBeanHolders);
        return result;
    }

    private static void logResolvedBeans(Set<BeanHolder<?>> registered, Set<BeanHolder<?>> additiveBeanHolders, Set<BeanHolder<?>> remainingBeanHolders) {
        if (log.isInfoEnabled()) {
            List<String> names = registered.stream()
                    .map(h -> h.getDescriptor().toShortString())
                    .sorted()
                    .collect(toList());
            log.info("Registered beans: {}", names);
        }
        Set<BeanHolder<?>> skipped = new LinkedHashSet<>();
        skipped.addAll(additiveBeanHolders);
        skipped.addAll(remainingBeanHolders);
        if (log.isDebugEnabled() && !skipped.isEmpty()) {
            List<String> names = skipped.stream()
                    .map(h -> h.getDescriptor().toShortString())
                    .sorted()
                    .collect(toList());
            log.debug("Skipped beans: {}", names);
        }
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
