package com.coditory.quark.context

import spock.lang.Specification

class PreconditionsSpec extends Specification {
    def "should fail non-null check"() {
        when:
            Preconditions.expectNonNull(null, "userName")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Expected non-null value: userName. Got: null"
    }

    def "should pass non-null check"() {
        given:
            String name = "John"
        expect:
            Preconditions.expectNonNull(name, "userName") == name
    }
}
