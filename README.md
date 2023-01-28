# Quark Context
[![Build](https://github.com/coditory/quark-context/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-context/actions/workflows/build.yml)
[![Coverage](https://codecov.io/github/coditory/quark-context/branch/master/graph/badge.svg?token=4VK4CVJVSN)](https://codecov.io/github/coditory/quark-context)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-context/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-context)

> Lightweight, single purpose, dependency injection java library. Similar to [IoC Container provided by Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans) but lighter.

- lightweight, exactly 3 minimalistic dependencies
- single purpose, not part of a framework
- provides both functional and annotation based API
- has conditional bean registration mechanism
- detects slow bean creation
- provides [event bus](https://github.com/coditory/quark-eventbus) integration
- public API annotated with `@NotNull` and `@Nullable` for better [kotlin integration](https://kotlinlang.org/docs/java-to-kotlin-nullability-guide.html#platform-types)

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-context:0.1.15"
}
```

## Loading application context

When using annotations you could load context with a single line:

```java
public class Application {
    public static void main(String[] args) {
        Context context = Context.scanPackage(Application.class);
        MyBean myBean = context.get(MyBean.class);
        // ...
    }
}
```

For more complicated setups use context builder:

```java
public class Application {
    public static void main(String[] args) {
        Context context = Context.builder()
                .add(new MyBean())
                .add(MyBean2.class, () -> new MyBean2())
                .add(MyBean3.class, (ctx) -> new MyBean3(context.getBean(MyBean.class)))
                .scanPackage(Application.class)
                .build();
        MyBean myBean = context.get(MyBean.class);
        // ...
    }
}
```
