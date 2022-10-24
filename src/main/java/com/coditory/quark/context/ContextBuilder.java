package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Bean;
import com.coditory.quark.context.annotations.Configuration;
import com.coditory.quark.context.events.ContextEventHandler;
import com.coditory.quark.eventbus.DispatchExceptionHandler;
import com.coditory.quark.eventbus.EventBus;
import com.coditory.quark.eventbus.EventBusBuilder;
import com.coditory.quark.eventbus.EventListener;
import com.coditory.quark.eventbus.Subscription;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static com.coditory.quark.context.BeanDescriptor.descriptor;
import static com.coditory.quark.context.BeanHolder.holder;
import static com.coditory.quark.context.ConstructorBasedBeanCreator.fromConstructor;
import static com.coditory.quark.context.Preconditions.expect;
import static com.coditory.quark.context.Preconditions.expectNonBlank;
import static com.coditory.quark.context.Preconditions.expectNonNull;

public final class ContextBuilder {
    public static final String CONTEXT_EVENT_BUS_NAME = "ContextEventBus";
    private final AtomicInteger DEFAULT_NAME_COUNTER = new AtomicInteger(1);
    private final Set<BeanHolder<?>> beanHolders = new LinkedHashSet<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final EventBusBuilder eventBusBuilder = EventBus.builder()
            .setName(CONTEXT_EVENT_BUS_NAME);
    private boolean initialized = false;
    private String name;
    private boolean registerContextEventBus = false;
    private boolean subscribeContextEventHandlers = true;
    private EventBus eventBus;
    private Duration beanCreationThreshold = Duration.ofMillis(50);
    private Duration beanTotalCreationThreshold = null;

    public ContextBuilder setName(@NotNull String name) {
        expectUninitialized();
        this.name = expectNonBlank(name, "name");
        return this;
    }

    public ContextBuilder registerContextEventBus() {
        return registerContextEventBus(true);
    }

    public ContextBuilder registerContextEventBus(boolean register) {
        expectUninitialized();
        this.registerContextEventBus = register;
        return this;
    }

    public ContextBuilder subscribeContextEventHandlers(boolean subscribe) {
        expectUninitialized();
        this.subscribeContextEventHandlers = subscribe;
        return this;
    }

    public ContextBuilder setEventBusExceptionHandler(DispatchExceptionHandler exceptionHandler) {
        expectUninitialized();
        expectNonNull(exceptionHandler, "exceptionHandler");
        expect(eventBus == null, "EventBus already initialized");
        eventBusBuilder.setExceptionHandler(exceptionHandler);
        return this;
    }

    public ContextBuilder subscribe(Subscription<?> listener) {
        expectUninitialized();
        eventBusBuilder.subscribe(listener);
        return this;
    }

    public ContextBuilder subscribe(Object listener) {
        expectUninitialized();
        eventBusBuilder.subscribe(listener);
        return this;
    }

    public <T> ContextBuilder subscribe(Class<? extends T> eventType, EventListener<T> listener) {
        expectUninitialized();
        eventBusBuilder.subscribe(eventType, listener);
        return this;
    }

    public ContextBuilder warnAboutSlowBeanCreation(Duration duration) {
        expectNonNull(duration, "duration");
        expect(!duration.isNegative(), "Expected non-negative duration");
        this.beanCreationThreshold = duration;
        return this;
    }

    public ContextBuilder warnAboutSlowBeanCreationWithDependencies(Duration duration) {
        expectNonNull(duration, "duration");
        expect(!duration.isNegative(), "Expected non-negative duration");
        this.beanTotalCreationThreshold = duration;
        return this;
    }

    public ContextBuilder scanPackage(Class<?> type) {
        expectNonNull(type, "type");
        return scanPackage(type.getPackageName(), name -> true);
    }

    public ContextBuilder scanPackage(Class<?> type, Predicate<String> canonicalNameFilter) {
        expectNonNull(type, "type");
        expectNonNull(canonicalNameFilter, "canonicalNameFilter");
        return scanPackage(type.getPackageName(), canonicalNameFilter);
    }

    public ContextBuilder scanPackage(String packageName) {
        expectNonNull(packageName, "packageName");
        return scanPackage(packageName, name -> true);
    }

    public ContextBuilder scanPackage(String packageName, Predicate<String> canonicalNameFilter) {
        expectNonNull(packageName, "packageName");
        expectNonNull(canonicalNameFilter, "canonicalNameFilter");
        Iterable<Class<?>> iterable = () -> ClasspathScanner.scanPackageAndSubPackages(packageName, canonicalNameFilter);
        StreamSupport.stream(iterable.spliterator(), false)
                .forEach(this::scanClass);
        return this;
    }

    public <T> ContextBuilder scanClass(Class<T> type) {
        expectNonNull(type, "type");
        Configuration configuration = type.getAnnotation(Configuration.class);
        if (configuration != null) {
            if (type.isAnnotationPresent(Bean.class)) {
                throw new ContextException("Detected incompatible annotations on class: " + type.getCanonicalName()
                        + ". Class should have one annotation: @Configuration or @Bean.");
            }
            addBeansFromConfiguration(configuration, type);
        } else if (type.isAnnotationPresent(Bean.class)) {
            addAnnotatedBeanClass(type);
        }
        return this;
    }

    private <T> void addAnnotatedBeanClass(Class<T> type) {
        Bean annotation = type.getAnnotation(Bean.class);
        BeanCreator<T> creator = fromConstructor(type);
        addAnnotatedCreator(annotation, type, creator);
    }

    private <T> void addBeansFromConfiguration(Configuration configuration, Class<T> type) {
        String name = configuration.name().isBlank()
                ? configuration.value()
                : configuration.name();
        BeanHolder<T> holder = holder(descriptor(type, name), fromConstructor(type), configuration.eager());
        addBeanHolder(holder);
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                method.setAccessible(true);
                addFromAnnotatedConfiguration(holder, method, method.getReturnType());
            }
        }
    }

    private <T> void addFromAnnotatedConfiguration(BeanHolder<?> holder, Method method, Class<T> returnType) {
        BeanCreator<T> creator = new MethodBasedBeanCreator<>(holder, method);
        Bean annotation = method.getAnnotation(Bean.class);
        addAnnotatedCreator(annotation, returnType, creator);
    }

    private <T> void addAnnotatedCreator(Bean annotation, Class<T> type, BeanCreator<T> creator) {
        String name = annotation.name().isBlank()
                ? annotation.value()
                : annotation.name();
        BeanHolder<T> holder = holder(descriptor(type, name), creator, annotation.eager());
        addBeanHolder(holder);
    }

    public ContextBuilder setProperty(String name, Object value) {
        expectNonNull(name, "name");
        expectNonNull(value, "value");
        properties.put(name, value);
        return this;
    }

    public ContextBuilder setProperties(Map<String, Object> properties) {
        expectNonNull(properties, "properties");
        this.properties.putAll(properties);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(T bean) {
        expectNonNull(bean, "bean");
        BeanDescriptor<T> descriptor = descriptor((Class<T>) bean.getClass());
        BeanHolder<T> holder = holder(descriptor, (ctx) -> bean, true);
        addBeanHolder(holder);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> ContextBuilder add(T bean, String name) {
        expectNonNull(bean, "bean");
        expectNonNull(name, "name");
        BeanDescriptor<T> descriptor = descriptor((Class<T>) bean.getClass(), name);
        BeanHolder<T> holder = holder(descriptor, (ctx) -> bean, true);
        addBeanHolder(holder);
        return this;
    }

    public <T> ContextBuilder add(Class<T> type, Predicate<ConditionContext> condition, BeanCreator<T> beanCreator) {
        expectNonNull(condition, "condition");
        expectNonNull(type, "type");
        expectNonNull(beanCreator, "beanCreator");
        return add(type, wrapBeanCreator(condition, beanCreator));
    }

    public <T> ContextBuilder add(Class<T> type, String name, Predicate<ConditionContext> condition, BeanCreator<T> beanCreator) {
        expectNonNull(condition, "condition");
        expectNonNull(type, "type");
        expectNonNull(name, "name");
        expectNonNull(beanCreator, "beanCreator");
        return add(type, name, wrapBeanCreator(condition, beanCreator));
    }

    private <T> BeanCreator<T> wrapBeanCreator(Predicate<ConditionContext> condition, BeanCreator<T> beanCreator) {
        return new BeanCreator<>() {
            @Override
            public T create(ResolutionContext context) {
                return beanCreator.create(context);
            }

            @Override
            public boolean isActive(ConditionContext context) {
                return beanCreator.isActive(context) && condition.test(context);
            }
        };
    }

    public <T> ContextBuilder add(Class<T> type, BeanCreator<T> beanCreator) {
        expectNonNull(beanCreator, "beanCreator");
        addBeanHolder(holder(descriptor(type), beanCreator));
        return this;
    }

    public <T> ContextBuilder add(Class<T> type, String name, BeanCreator<T> beanCreator) {
        expectNonNull(name, "name");
        expectNonNull(beanCreator, "beanCreator");
        addBeanHolder(holder(descriptor(type, name), beanCreator));
        return this;
    }

    private void addBeanHolder(BeanHolder<?> holder) {
        beanHolders.add(holder);
    }

    public Context buildEager() {
        return build(() -> Context.createEager(name, beanHolders, properties, eventBus));
    }

    public Context build() {
        return build(() -> Context.create(name, beanHolders, properties, eventBus));
    }

    private Context build(Supplier<Context> contextCreator) {
        initialize();
        Context context = contextCreator.get();
        subscribeContextEventHandlers(context);
        return context;
    }

    private void initialize() {
        expectUninitialized();
        initializeName();
        initializeEventBus();
        initialized = true;
    }

    private void initializeName() {
        expectUninitialized();
        int counter = DEFAULT_NAME_COUNTER.incrementAndGet();
        String defaultName = "Context";
        name = counter > 1 ? defaultName + "-" + counter : defaultName;
    }

    private void initializeEventBus() {
        expectUninitialized();
        if (beanCreationThreshold != null || beanTotalCreationThreshold != null) {
            eventBusBuilder.subscribe(new SlowBeanCreationDetector(beanCreationThreshold, beanTotalCreationThreshold));
        }
        eventBus = eventBusBuilder.build();
        if (registerContextEventBus) {
            add(eventBus, eventBus.getName());
        }
        for (BeanHolder<?> holder : beanHolders) {
            holder.setEventEmitter(eventBus);
        }
    }

    private void subscribeContextEventHandlers(Context context) {
        if (!subscribeContextEventHandlers) {
            return;
        }
        for (BeanHolder<?> holder : beanHolders) {
            if (holder.getBeanType().isAnnotationPresent(ContextEventHandler.class)) {
                Object bean = context.get(holder.getDescriptor());
                eventBus.subscribe(bean);
            }
        }
    }

    private void expectUninitialized() {
        if (initialized) {
            throw new IllegalStateException("Modifications forbidden - context builder was already initialized");
        }
    }
}
