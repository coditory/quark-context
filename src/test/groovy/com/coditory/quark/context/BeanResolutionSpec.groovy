package com.coditory.quark.context

import spock.lang.Specification

class BeanResolutionSpec extends Specification {
    def "should retrieve named bean by type"() {
        given:
            Context context = Context.builder()
                    .add(new Bar(), "BAR")
                    .build()
        when:
            Bar bar = context.getOrNull(Bar)
        then:
            bar != null
    }

    def "should fail registering multiple beans with the same name"() {
        when:
            Context.builder()
                    .add(new Bar(), "BAR")
                    .add(new Baz(), "BAR")
                    .build()
        then:
            ContextException e = thrown(ContextException)
            e.message == "Duplicated bean name: BAR"
    }

    def "should retrieve unnamed bean by type when multiple named beans are registered"() {
        given:
            Bar unnamed = new Bar()
            Context context = Context.builder()
                    .add(new Bar(), "BAR")
                    .add(unnamed)
                    .add(new Bar(), "BAR2")
                    .build()
        when:
            Bar bar = context.getOrNull(Bar)
        then:
            bar == unnamed
    }

    def "should fail to retrieve bean by type when multiple named beans are registered"() {
        given:
            Context context = Context.builder()
                    .add(new Bar(), "BAR")
                    .add(new Bar(), "BAR2")
                    .build()
        when:
            context.getOrNull(Bar)
        then:
            ContextException e = thrown(ContextException)
            e.message == "Expected single bean: Bar. Found 2 beans."
    }

    def "should fail to retrieve bean by type when multiple unnamed beans are registered"() {
        given:
            Context context = Context.builder()
                    .add(new Bar())
                    .add(new Bar())
                    .build()
        when:
            context.getOrNull(Bar)
        then:
            ContextException e = thrown(ContextException)
            e.message == "Expected single bean: Bar. Found 2 beans."
    }

    def "should retrieve named bean by type and name"() {
        given:
            Context context = Context.builder()
                    .add(new Bar(), "BAR")
                    .build()
        when:
            Bar bar = context.getOrNull(Bar, "BAR")
        then:
            bar != null
    }

    def "should not retrieve unnamed bean by name"() {
        given:
            Context context = Context.builder()
                    .add(new Bar())
                    .build()
        when:
            Bar bar = context.getOrNull(Bar, "BAR")
        then:
            bar == null
    }

    def "should not retrieve named bean by a different name"() {
        given:
            Context context = Context.builder()
                    .add(new Bar(), "BAR")
                    .build()
        when:
            Bar bar = context.getOrNull(Bar, "BARR")
        then:
            bar == null
    }

    def "should retrieve all named beans by type"() {
        given:
            Context context = Context.builder()
                    .add(new Bar(), "BAR1")
                    .add(new Bar())
                    .add(new Bar(), "BAR3")
                    .build()
        when:
            List<Bar> bars = context.getAllOrEmpty(Bar)
        then:
            bars.size() == 3
    }

    class Bar {}

    class Baz {}
}
