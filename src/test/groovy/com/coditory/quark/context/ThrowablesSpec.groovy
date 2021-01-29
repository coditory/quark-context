package com.coditory.quark.context

import spock.lang.Specification

class ThrowablesSpec extends Specification {
    def "should extract root cause of specific type or null"() {
        given:
            Throwable root = new RuntimeException("root")
            Throwable exceptionA = new IllegalArgumentException("A", root)
            Throwable exceptionB = new IllegalArgumentException("B", exceptionA)

        when:
            Throwable result = Throwables.getRootCauseOfType(exceptionB, IllegalArgumentException)
        then:
            result == exceptionA

        when:
            result = Throwables.getRootCauseOfType(exceptionB, IllegalStateException)
        then:
            result == null
    }
}
