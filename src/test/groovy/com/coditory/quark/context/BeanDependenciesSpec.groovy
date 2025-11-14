package com.coditory.quark.context

import spock.lang.Specification

class BeanDependenciesSpec extends Specification {
    def "should register bean with dependency on the dependency context"() {
        given:
            Context context = Context.builder()
                    .add(new Baz())
                    .add(Bar.class, { new Bar(it.get(Context.class)) })
                    .add(new Foo())
                    .buildEager()

        when:
            Bar bar = context.get(Bar)
        then:
            bar.ctx != null
            bar.baz != null
            bar.foo != null
        and:
            bar.ctx.get(Baz) != null
            bar.ctx.get(Foo) != null
    }

    class Baz {}

    class Foo {}

    class Bar {
        Context ctx
        Baz baz
        Foo foo

        Bar(Context ctx) {
            this.ctx = ctx
            this.baz = ctx.getOrNull(Baz.class)
            this.foo = ctx.getOrNull(Foo.class)
        }
    }
}
