# Quark Context

[![Build Status](https://github.com/coditory/quark-context/workflows/Build/badge.svg?branch=master)](https://github.com/coditory/quark-context/actions?query=workflow%3ABuild+branch%3Amaster)
[![Coverage Status](https://coveralls.io/repos/github/coditory/quark-context/badge.svg?branch=master)](https://coveralls.io/github/coditory/quark-context?branch=master)
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
    compile 'com.coditory.quark:quark-context:0.1.2'
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