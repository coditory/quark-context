package com.coditory.quark.context.annotated

import com.coditory.quark.context.Context
import com.coditory.quark.context.annotated.samples.beans.Foo
import com.coditory.quark.context.annotated.samples.beans.bar.Bar
import com.coditory.quark.context.annotated.samples.beans.bar.Baz
import com.coditory.quark.context.annotated.samples.beans.bar.Foo as Foo2
import spock.lang.Specification

class AnnotatedBeansSpec extends Specification {
    def "should load annotated beans"() {
        when:
            Context context = Context.builder()
                    .scanPackageAndSubPackages(Foo.class)
                    .build()
        then:
            context.getOrNull(Foo) != null
            context.getOrNull(Bar) != null
            context.getOrNull(Foo2) != null
        and:
            context.getOrNull(Baz) == null
    }
}
