package annotated

import annotated.samples.config_init.ConfigEagerInit
import annotated.samples.config_init.ConfigInit
import annotated.samples.config_inject.ConfigInject
import com.coditory.quark.context.Context
import spock.lang.Specification

class ConfigurationRegistrationSpec extends Specification {
    def "should register beans from a configuration"() {
        given:
            Context context = Context.scanPackage(annotated.samples.config.SampleConfig)

        when:
            annotated.samples.config.Foo foo = context.get(annotated.samples.config.Foo)
        then:
            foo.name == "no-name"
            foo.bar != null

        when:
            annotated.samples.config.Foo foooo = context.get(annotated.samples.config.Foo, "foooo")
        then:
            foooo.name == "foooo"
            foooo.bar != null

        when:
            annotated.samples.config.Foo foo2 = context.get(annotated.samples.config.Foo, "foo2")
        then:
            foo2.name == "foo2"
            foo2.bar != null
    }

    def "should inject bean into configuration constructor"() {
        given:
            Context context = Context.scanPackage(ConfigInject)

        when:
            annotated.samples.config_inject.Baz baz = context.get(annotated.samples.config_inject.Baz)
        then:
            baz.bar != null
    }

    def "should initialize configuration before creating a bean defined by it"() {
        when:
            Context context = Context.scanPackage(ConfigInit)
        then:
            ConfigInit.initialized == false
            ConfigInit.bar == null

        when:
            annotated.samples.config_init.Baz baz = context.get(annotated.samples.config_init.Baz)
        then:
            baz.bar != null
        and:
            ConfigInit.initialized == true
            ConfigInit.bar != null
    }

    def "should initialize eager configuration right after context is built"() {
        when:
            Context.scanPackage(ConfigEagerInit)
        then:
            ConfigEagerInit.initialized == true
            ConfigEagerInit.bar != null
    }
}
