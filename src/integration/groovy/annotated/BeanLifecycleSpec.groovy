package annotated

import annotated.samples.beans_lifecycle.ConfigLifecycle
import com.coditory.quark.context.Context
import spock.lang.Specification

class BeanLifecycleSpec extends Specification {
    def "should register beans and initialize them"() {
        given:
            Context context = Context.scanPackage(ConfigLifecycle)

        when:
            ConfigLifecycle config = context.get(ConfigLifecycle)
        then:
            config.initialized
            config.initialized2
        and:
            !config.finalized
            !config.finalized2

        when:
            annotated.samples.beans_lifecycle.Bar bar = context.get(annotated.samples.beans_lifecycle.Bar)
        then:
            bar.initialized
            !bar.finalized

        when:
            annotated.samples.beans_lifecycle.Baz baz = context.get(annotated.samples.beans_lifecycle.Baz)
        then:
            baz.initialized
            !baz.finalized
    }

    def "should finalize beans when closing the context"() {
        given:
            Context context = Context.scanPackage(ConfigLifecycle)
        and:
            ConfigLifecycle config = context.get(ConfigLifecycle)
            annotated.samples.beans_lifecycle.Bar bar = context.get(annotated.samples.beans_lifecycle.Bar)
            annotated.samples.beans_lifecycle.Baz baz = context.get(annotated.samples.beans_lifecycle.Baz)
        when:
            context.close()
        then:
            config.finalized
            config.finalized2
        and:
            bar.finalized
        and:
            baz.finalized
    }
}
