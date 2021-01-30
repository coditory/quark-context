package com.coditory.quark.context

import spock.lang.Specification

class BeanConditionsSpec extends Specification {
    def "should register bean with condition on a property"() {
        given:
            Context context = Context.builder()
                    .add(Bar.class, { it.getProperty("bar.enabled") == true }, { new Bar() })
                    .setProperty("bar.enabled", true)
                    .build()

        when:
            Bar bar = context.getOrNull(Bar)
        then:
            bar != null
    }

    def "should not register bean with condition on a missing property"() {
        given:
            Context context = Context.builder()
                    .add(Bar.class, { it.getProperty("bar.enabled") == true }, { new Bar() })
                    .build()

        when:
            Bar bar = context.getOrNull(Bar)
        then:
            bar == null
    }

    def "should register bean Foo when Bar -> Foo && Foo -> !Bar"() {
        given:
            Context context = Context.builder()
                    .add(Bar.class, { it.hasBean(Foo) }, { new Bar() })
                    .add(Foo.class, { !it.hasBean(Bar) }, { new Foo() })
                    .build()

        expect:
            context.getOrNull(Bar) == null
            context.getOrNull(Foo) != null
    }

    def "should register bean Foo, Bar, Baz when Bar -> Foo -> Baz -> prop(baz.enabled)"() {
        given:
            Context context = Context.builder()
                    .add(Baz.class, { it.getProperty("baz.enabled") == true }, { new Baz() })
                    .add(Foo.class, { it.hasBean(Baz) }, { new Foo() })
                    .add(Bar.class, { it.hasBean(Foo) }, { new Bar() })
                    .setProperty("baz.enabled", true)
                    .build()

        expect:
            context.getOrNull(Baz) != null
            context.getOrNull(Foo) != null
            context.getOrNull(Bar) != null
    }

    def "should register Baz, Foo when Bar -> Foo -> Baz -> !Bar"() {
        given:
            Context context = Context.builder()
                    .add(Baz.class, { !it.hasBean(Bar) }, { new Baz() })
                    .add(Foo.class, { it.hasBean(Baz) }, { new Foo() })
                    .add(Bar.class, { it.hasBean(Foo) }, { new Bar() })
                    .build()

        expect:
            context.getOrNull(Baz) != null
            context.getOrNull(Foo) != null
            context.getOrNull(Bar) == null
    }

    class Baz {}

    class Bar {}

    class Foo {}
}
