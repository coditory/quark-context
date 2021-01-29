package com.coditory.quark.context

import spock.lang.Specification

class ArgsSpec extends Specification {
    def "should fail non-null check"() {
        when:
            Args.checkNonNull(null, "userName")
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Expected non-null value: userName. Got: null"
    }

    def "should pass non-null check"() {
        given:
            String name = "John"
        expect:
            Args.checkNonNull(name, "userName") == name
    }
}
