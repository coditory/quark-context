package annotated

import com.coditory.quark.context.Context
import spock.lang.Specification

class BeanConditionsSpec extends Specification {
    def "should load one of beans that exclusively depend on each other"() {
        when:
            Context context = Context.scanPackage(annotated.samples.condition_circular.Foo.class)
        then:
            context.getOrNull(annotated.samples.condition_circular.Foo) != null
            context.getOrNull(annotated.samples.condition_circular.Bar) == null
    }

    def "should load beans that transitively depend on each other"() {
        when:
            Context context = Context.scanPackage(annotated.samples.condition_on_bean.Foo.class)
        then:
            context.getOrNull(annotated.samples.condition_on_bean.Foo) != null
            context.getOrNull(annotated.samples.condition_on_bean.Bar) != null
            context.getOrNull(annotated.samples.condition_on_bean.Baz) != null
    }

    def "should load beans with class dependencies"() {
        when:
            Context context = Context.scanPackage(annotated.samples.condition_on_class.Foo.class)
        then:
            context.getOrNull(annotated.samples.condition_on_class.Foo) != null
            context.getOrNull(annotated.samples.condition_on_class.Baz) != null
            context.getOrNull(annotated.samples.condition_on_class.Bar) == null
    }

    def "should load beans that depend on missing other bean"() {
        when:
            Context context = Context.scanPackage(annotated.samples.condition_on_missing_bean.Foo.class)
        then:
            context.getOrNull(annotated.samples.condition_on_missing_bean.Baz) != null
            context.getOrNull(annotated.samples.condition_on_missing_bean.Bar) == null
    }

    def "should load beans that depend on property"() {
        when:
            Context context = Context.builder()
                    .scanPackage(annotated.samples.condition_on_property.Foo.class)
                    .setProperty("foo.enabled", true)
                    .build()
        then:
            context.getOrNull(annotated.samples.condition_on_property.Foo) != null
            context.getOrNull(annotated.samples.condition_on_property.Baz) != null
            context.getOrNull(annotated.samples.condition_on_property.Bar) == null
    }

    def "should load beans with condition on disabled property"() {
        when:
            Context context = Context.builder()
                    .scanPackage(annotated.samples.condition_on_property.Foo.class)
                    .setProperty("foo.enabled", false)
                    .build()
        then:
            context.getOrNull(annotated.samples.condition_on_property.Foo) == null
            context.getOrNull(annotated.samples.condition_on_property.Baz) == null
            context.getOrNull(annotated.samples.condition_on_property.Bar) != null
    }

    def "should load beans with condition on missing property"() {
        when:
            Context context = Context.scanPackage(annotated.samples.condition_on_property.Foo.class)
        then:
            context.getOrNull(annotated.samples.condition_on_property.Foo) == null
            context.getOrNull(annotated.samples.condition_on_property.Baz) == null
            context.getOrNull(annotated.samples.condition_on_property.Bar) != null
    }
}
