# Quark Config
[![Build Status](https://github.com/coditory/quark-config/workflows/Build/badge.svg?branch=master)](https://github.com/coditory/quark-config/actions?query=workflow%3ABuild+branch%3Amaster)
[![Coverage Status](https://coveralls.io/repos/github/coditory/quark-config/badge.svg?branch=master)](https://coveralls.io/github/coditory/quark-config?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-config/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-config)
[![JavaDoc](http://www.javadoc.io/badge/com.coditory.quark/quark-config.svg)](http://www.javadoc.io/doc/com.coditory.quark/quark-config)
[![Join the chat at https://gitter.im/coditory/quark-config](https://badges.gitter.im/coditory/coditory-common.svg)](https://gitter.im/coditory/quark-config?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

> Quark Config is a lightweight and single purpose java library for loading and manipulating configurations

The idea behind was to create a configuration library,
similar to the one created in [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
of [typesafe](https://github.com/lightbend/config), that is:
- lightweight, without a burden of a framework
- supports standard YAML or JSON format
- provides developer-friendly API
- hides secrets when formatting to string
- provides basic expressions for references and default values
- provides a collection of parsers for values such as `java.util.Duration`
- loads an application multi source config with a one-liner

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    compile 'com.coditory.quark:quark-config:0.1.0'
}
```

## Usage

### Load application configuration

In most cases application should load the config with a one-liner:

```java
public class Application {
    public static void main(String[] args) {
        Config config = ConfigFactory.loadApplicationConfig(args);
        System.out.println(config);
        // Sample output:
        // {application={port=8080, name=best-app}, db={password=***}}
    }
}
```

#### Config merging order

Application configuration sources are merged into a single config object 
according to the following order:
- base config
  - `application.{yml,json,properties}` file from classpath
  - this is the only file that is required
- profile config
  - `application-${profile}.{yml,json,properties}` file from classpath 
  - set profile with argument `--profile=$PROFILE`
  - the default profile is `local`
- external config
  - config file from system passed as an argument `--config.external=$PATH_TO_CONFIG`
- config values from arguments
  - all arguments passed as `--config.<config_key>=$VALUE` become a config value

Example:
```yml
# file: application.yml
application:
  name: "best-app"
  port: ${env.PORT ? args.port ? 7070}

# file: application-local.yml
application:
  port: 8080

# file: application-prod.yml
application:
  port: 80
```

```java
// load config with no arguments
ConfigFactory.loadApplicationConfig();
// Output: {application={port=8080, name=best-app}}

// load config with prod profile
ConfigFactory.loadApplicationConfig("--profile", "prod");
// Output: {application={port=80, name=best-app}}

// load config with non-existent profile
ConfigFactory.loadApplicationConfig("--profile", "other");
// Output: {application={port=7070, name=best-app}}

// resolve config with expression
ConfigFactory.loadApplicationConfig("--profile", "other", "--port", "7071");
// Output: {application={port=7071, name=best-app}}

// load config with config argument
ConfigFactory.loadApplicationConfig("--config.application.port", "8081");
// Output: {application={port=8081, name=best-app}}
```
For more examples see the [test cases](src/integration/groovy/com/coditory/quark/config/LoadApplicationConfigSpec.groovy).

#### Config resolution variables

You can use following expression variables to resolve an application config:
- `${env.*}` - all system variables from `System.getenv()`
- `${system.*}` - all system variables from `System.getProperties()`
- `${args.*}` - all arguments

### Creating a config

You can create a config object in multiple ways:

```java
// Create from var args
Config.of(
    "application.name", "best-app",
    "application.port", 8080
);

// Create from map
Config.of(Map.of("application.port", 8080));

// Create empty config and add value
Config.empty()
    .withValue("application.port", 8080);

// Build own config with customizations
Config.builder()
    .withValue("application.name", "best-app")
    .withValue("application.port", 8080)
    .withSecretHidingValueMapper(customSecretHider)
    .withValueParser(customParser)
    .build();
```

Config object is immutable. Every time you add a value to a config, new instance is returned:
```java
Config c1 = Config.of("application.port", 8080);
Config c2 = c1.withValue("application.name", "best-app");

assert c1 != c2
assert !c1.equals(c2)
```

### Resolving a config value
```java
// throws if config value is not defined
config.getInteger("application.port")

// returns a default value if config value is not defined
config.getInteger("application.port", 8080)

// returns null if config value is not defined
config.getIntegerOrNull("application.port")

// returns an Optional from config value
config.getIntegerAsOptional("application.port")
```

Quark Config provides a lot of parsers
so String values may be retrieved as a non String objects:
```java
Config.of(
    "port", "8080",
    "timeout", "1s",
    "locale", "pl-PL",
    "currency", "PLN",
    "timestamp", "2007-12-03T10:15:30.00Z"
)
assert config.getInteger("port") == 8080
assert config.getInteger("timeout") == Duration.parse("PT1S")
assert config.getInteger("locale") == new Locale("pl", "PL")
assert config.getInteger("currency") == Currency.getInstance("PLN")
assert config.getInteger("timestamp") == Instant.parse("2007-12-03T10:15:30.00Z")
```

Resolving a subconfig:
```java
Config applicationConfig = config.getSubConfig("application")
applicationConfig.getInteger("port")
```

### Merging configs
Two config objects can be merged together:

```java
Config config = Config.of(
    "a", "A",
    "b", "B"
);
Config other = Config.of(
   "b", "X",
   "c", "Y"
);

config.withDefaults(other);
// {a=A, b=B, c=Y}

config.withValues(other);
// {a=A, b=X, c=Y}
```

### Config expressions

Config values can use som basic expressions:
```java
// Config with references to other value
Config.builder()
    .withValue("a", "${b}")
    .withValue("b", "B")
    .build()
    .resolveExpressions();
// {a=B, b=B}

// Config with a fallback value
Config.builder()
    .withValue("a", "${b ? X}")
    .build()
    .resolveExpressions();
// {a=X}

// Config with a double fallback value
Config
    .of("a", "${b ? c ? X}")
    .resolveExpressions();
// {a=X}

// Config with expression values
Config
    .of("a", "${b ? c ? X}")
    .resolveExpressions(Config.of(c, "C"));
// {a=C}
```

### Load config from file

Config can be loaded from:
- classpath file
- file from file system

You don need to specify extension of the configuration file.
Quark Config will automatically look for: `*.yml`, `*.yaml`, `*.json`, `*.properties`
```java
ConfigFactory.loadFromClasspath("custom-config");
ConfigFactory.loadFromFileSystem("custom-config");
```

### Parsing and formatting a config

Quarkus Config supports following formats:
- yaml
- json
- properties

```java
// parsing a config
ConfigFactory.parseYaml(yaml);
ConfigFactory.parseJson(json);
ConfigFactory.parseProperties(properties);

// fomatting a config
ConfigFormatter.toYaml(config);
ConfigFormatter.toJson(config);
ConfigFormatter.toProperties(config);

// fomatting a config with exposed secrets
ConfigFormatter.toYamlWithExposedSecrets(config);
ConfigFormatter.toJsonWithExposedSecrets(config);
ConfigFormatter.toPropertiesWithExposedSecrets(config);

// config.toString() method uses output of a Map with hidden secrets
config.toString()
```
