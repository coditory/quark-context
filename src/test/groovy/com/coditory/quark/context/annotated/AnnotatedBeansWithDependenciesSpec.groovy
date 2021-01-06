package com.coditory.quark.context.annotated

import com.coditory.quark.context.Context
import com.coditory.quark.context.annotated.samples.beans_with_deps.Bar
import com.coditory.quark.context.annotated.samples.beans_with_deps.Baz
import com.coditory.quark.context.annotated.samples.beans_with_deps.Foo
import spock.lang.Specification

class AnnotatedBeansWithDependenciesSpec extends Specification {
    def "should load annotated beans with dependencies"() {
        when:
            Context context = Context.builder()
                    .scanPackageAndSubPackages(AnnotatedBeansWithDependenciesSpec)
                    .build()
        then:
            context.get(Foo)
            context.get(Bar)
            context.get(Baz)
    }
}
