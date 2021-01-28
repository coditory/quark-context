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
            e.message == "Could not create bean: ${annotated.samples.optional_multiple_deps.Baz.class.canonicalName}"
            e.cause.message == "Beans not found for type: ${annotated.samples.optional_multiple_deps.Foo.class.canonicalName}"
    }

    def "should fail injecting named list of beans"() {
        given:
            Context context = Context.scanPackage(annotated.samples.named_multiple_deps.Bar)
        when:
            context.get(annotated.samples.named_multiple_deps.Bar)

        then:
            ContextException e = thrown(ContextException)
            e.message == "Could not create bean: ${annotated.samples.named_multiple_deps.Bar.class.canonicalName}"
            e.cause.message.startsWith("Detected named @Dependency for a list of dependencies")
    }
}
