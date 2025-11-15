package annotated

import com.coditory.quark.context.Context
import com.coditory.quark.context.ContextException
import spock.lang.Specification

class InjectMultipleDependenciesSpec extends Specification {
    def "should inject list of beans"() {
        given:
            Context context = Context.scanPackage(annotated.samples.multiple_deps.Bar)
        when:
            annotated.samples.multiple_deps.Bar bar = context.get(annotated.samples.multiple_deps.Bar)

        then:
            bar.foo != null
            bar.foo.size() == 2
    }

    def "should inject list of beans with extends"() {
        given:
            Context context = Context.scanPackage(annotated.samples.multiple_deps.Bar2)
        when:
            annotated.samples.multiple_deps.Bar2 bar = context.get(annotated.samples.multiple_deps.Bar2)

        then:
            bar.foo != null
            bar.foo.size() == 2
    }

    def "should inject empty list for no beans and optional dependency"() {
        given:
            Context context = Context.scanPackage(annotated.samples.optional_multiple_deps.Bar)
        when:
            annotated.samples.optional_multiple_deps.Bar bar = context.get(annotated.samples.optional_multiple_deps.Bar)

        then:
            bar.foo != null
            bar.foo.isEmpty()
    }

    def "should fail injecting required list of no beans"() {
        given:
            Context context = Context.scanPackage(annotated.samples.optional_multiple_deps.Baz)
        when:
            context.get(annotated.samples.optional_multiple_deps.Baz)

        then:
            ContextException e = thrown(ContextException)
            e.message == "Could not create bean: Baz"
            e.cause.message == "Could not create bean from constructor: public annotated.samples.optional_multiple_deps.Baz(java.util.List)"
            e.cause.cause.message == "Beans not found for type: Foo"
    }

    def "should fail injecting named list of beans"() {
        given:
            Context context = Context.scanPackage(annotated.samples.named_multiple_deps.Bar)
        when:
            context.get(annotated.samples.named_multiple_deps.Bar)

        then:
            ContextException e = thrown(ContextException)
            e.message == "Could not create bean: Bar"
            e.cause.message == "Could not create bean from constructor: public annotated.samples.named_multiple_deps.Bar(java.util.List)"
            e.cause.cause.message.startsWith("Detected named @Dependency for a list of dependencies")
    }
}
