package com.coditory.quark.context.annotated

import com.coditory.quark.context.Context
import com.coditory.quark.context.ContextException
import com.coditory.quark.context.annotated.samples.beans_circular_deps.Bar
import spock.lang.Specification

class AnnotatedBeansWithCircularDependenciesSpec extends Specification {
    def "should throw descriptive error when creating beans with circular dependency"() {
        given:
            Context context = Context.builder()
                    .scanPackageAndSubPackages(AnnotatedBeansWithCircularDependenciesSpec)
                    .build()
        when:
            context.get(Bar)

        then:
            ContextException e = thrown(ContextException)
            e.message == ""
    }
}
