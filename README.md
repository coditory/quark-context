# Quark Context
[![Build](https://github.com/coditory/quark-context/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-context/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/github/coditory/quark-context/badge.svg)](https://coveralls.io/github/coditory/quark-context)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-context/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-context)

> Quark Context is a lightweight and single purpose java library for loading and manipulating configurations

The idea was to create a small, single-jar library, similar to
the [IoC Container provided by Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans)
, that is:

- lightweight, no dependencies
- single purpose and is not part of a framework
- provides both functional and annotation based API
- has conditional bean registration mechanism
- detects slow bean creation

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-context:0.1.3"
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
