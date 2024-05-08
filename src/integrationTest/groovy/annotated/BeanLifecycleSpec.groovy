package annotated

import annotated.samples.beans_lifecycle.ConfigLifecycle
import annotated.samples.beans_lifecycle.EagerBar
import com.coditory.quark.context.Context
import spock.lang.Specification

class BeanLifecycleSpec extends Specification {
    def "should lazy initialize beans"() {
        when:
            Context context = Context.scanPackage(ConfigLifecycle)
        then:
            annotated.samples.beans_lifecycle.Bar.initialized == false
            annotated.samples.beans_lifecycle.Bar.postInitialized == false
            annotated.samples.beans_lifecycle.Baz.initialized == false
            annotated.samples.beans_lifecycle.Baz.postInitialized == false

        when:
            context.get(annotated.samples.beans_lifecycle.Bar)
        then:
            annotated.samples.beans_lifecycle.Bar.initialized
            annotated.samples.beans_lifecycle.Bar.postInitialized

        when:
            context.get(annotated.samples.beans_lifecycle.Baz)
        then:
            annotated.samples.beans_lifecycle.Baz.initialized
            annotated.samples.beans_lifecycle.Baz.postInitialized
    }

    def "should initialize eager bean right after context is built"() {
        when:
            Context.scanPackage(EagerBar)
        then:
            EagerBar.initialized == true
            EagerBar.postInitialized == true
    }

    def "should finalize beans when closing the context"() {
        given:
            Context context = Context.builder()
                    .scanPackage(ConfigLifecycle)
                    .build()
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
