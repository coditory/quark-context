package annotated

import com.coditory.quark.context.Context
import com.coditory.quark.context.ContextException
import spock.lang.Specification

class BeanRegistrationSpec extends Specification {
    def "should load annotated beans"() {
        when:
            Context context = Context.scanPackage(annotated.samples.beans.Foo.class)
        then:
            context.getOrNull(annotated.samples.beans.Foo) != null
            context.getOrNull(annotated.samples.beans.bar.Bar) != null
            context.getOrNull(annotated.samples.beans.bar.Foo) != null
        and:
            context.getOrNull(annotated.samples.beans.bar.Baz) == null
    }

    def "should load annotated beans with dependencies"() {
        when:
            Context context = Context.scanPackage(annotated.samples.beans_with_deps.Foo)
        then:
            context.get(annotated.samples.beans_with_deps.Foo)
            context.get(annotated.samples.beans_with_deps.Bar)
            context.get(annotated.samples.beans_with_deps.Baz)
    }

    def "should throw error when creating beans with circular dependency"() {
        given:
            Context context = Context.scanPackage(annotated.samples.beans_circular_deps.Foo)
        when:
            context.get(annotated.samples.beans_circular_deps.Bar)

        then:
            ContextException e = thrown(ContextException)
            e.message == "Could not create bean: Bar"
            e.cause.message == "Detected cyclic dependency: Bar -> Baz -> Foo -> Bar"
    }

    def "should throw error when creating bean with a self dependency"() {
        given:
            Context context = Context.scanPackage(annotated.samples.beans_circular_deps.BarBar)
        when:
            context.get(annotated.samples.beans_circular_deps.BarBar)

        then:
            ContextException e = thrown(ContextException)
            e.message == "Could not create bean: BarBar"
            e.cause.message == "Detected cyclic dependency: BarBar -> BarBar"
    }

    def "should inject named beans"() {
        given:
            Context context = Context.scanPackage(annotated.samples.named_bean.Bar)
        when:
            annotated.samples.named_bean.Bar bar = context.get(annotated.samples.named_bean.Bar)

        then:
            bar.foo != null
            bar.baz == null
    }

    def "should inject optional beans"() {
        given:
            Context context = Context.scanPackage(annotated.samples.optional_bean.Bar)
        when:
            annotated.samples.optional_bean.Bar bar = context.get(annotated.samples.optional_bean.Bar)

        then:
            bar.foo == null
            bar.baz != null
    }

    def "should inject dependencies by interface"() {
        given:
            Context context = Context.scanPackage(annotated.samples.optional_bean.Bar)
        when:
            annotated.samples.optional_bean.Bar bar = context.get(annotated.samples.optional_bean.Bar)

        then:
            bar.foo == null
            bar.baz != null
    }
}
