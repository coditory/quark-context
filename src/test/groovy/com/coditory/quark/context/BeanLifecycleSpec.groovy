package com.coditory.quark.context

import com.coditory.quark.context.annotations.Close
import com.coditory.quark.context.annotations.Init
import spock.lang.Specification

class BeanLifecycleSpec extends Specification {
    def "should register beans and initialize them"() {
        given:
            Context context = Context.builder()
                    .add(new LifecycleAnnotated())
                    .add(new LifecycleImplemented())
                    .build()

        when:
            LifecycleAnnotated lifecycleAnnotated = context.get(LifecycleAnnotated)
        then:
            lifecycleAnnotated.initialized
            lifecycleAnnotated.initialized2
            !lifecycleAnnotated.finalized

        when:
            LifecycleImplemented lifecycleImplemented = context.get(LifecycleImplemented)
        then:
            lifecycleImplemented.initialized
            !lifecycleImplemented.finalized
    }

    def "should finalize beans when closing the context"() {
        given:
            Context context = Context.builder()
                    .add(new LifecycleAnnotated())
                    .add(new LifecycleImplemented())
                    .build()
        and:
            LifecycleAnnotated lifecycleAnnotated = context.get(LifecycleAnnotated)
            LifecycleImplemented lifecycleImplemented = context.get(LifecycleImplemented)
        when:
            context.close()
        then:
            lifecycleAnnotated.finalized
            lifecycleAnnotated.finalized2
        and:
            lifecycleImplemented.finalized
    }

    def "should initialize eager beans after context is built"() {
        given:
            LifecycleAnnotated lifecycleAnnotated = new LifecycleAnnotated()
            LifecycleImplemented lifecycleImplemented = new LifecycleImplemented()
        and:
            Context context = Context.builder()
                    .add(lifecycleAnnotated)
                    .add(lifecycleImplemented)
                    .build()
        when:
            context.close()
        then:
            lifecycleAnnotated.initialized
            lifecycleImplemented.initialized
    }

    def "should not initialize lazy beans that were not retrieved from the context"() {
        given:
            LifecycleAnnotated lifecycleAnnotated = new LifecycleAnnotated()
            LifecycleImplemented lifecycleImplemented = new LifecycleImplemented()
        and:
            Context context = Context.builder()
                    .add(() -> lifecycleAnnotated)
                    .add(() -> lifecycleImplemented)
                    .build()
        when:
            context.close()
        then:
            !lifecycleAnnotated.initialized
            !lifecycleImplemented.initialized
    }

    def "should inject dependencies to lifecycle annotated methods"() {
        given:
            LifecycleAnnotated lifecycleAnnotated = new LifecycleAnnotated()
            LifecycleImplemented lifecycleImplemented = new LifecycleImplemented()
        and:
            Context context = Context.builder()
                    .add(lifecycleAnnotated)
                    .add(lifecycleImplemented)
                    .add(new LifecycleAnnotatedWithDeps())
                    .build()
        and:
            LifecycleAnnotatedWithDeps lifecycleAnnotatedWithDeps = context.get(LifecycleAnnotatedWithDeps)
        when:
            context.close()
        then:
            lifecycleAnnotatedWithDeps.initDependency == lifecycleAnnotated
            lifecycleAnnotatedWithDeps.finalizeDependency == lifecycleImplemented
    }

    def "should throw error when dependency for @Init annotated method is missing"() {
        when:
            Context.builder()
                    .add(new LifecycleAnnotatedWithDeps())
                    .build()
        then:
            BeanInitializationException e = thrown(BeanInitializationException)
            e.message.startsWith("Could not initialize bean: LifecycleAnnotatedWithDeps")
    }

    def "should throw error when dependency for @Close annotated method is missing"() {
        given:
            Context context = Context.builder()
                    .add(new LifecycleAnnotatedWithDeps())
                    .add(new LifecycleAnnotated())
                    .build()
        when:
            context.get(LifecycleAnnotatedWithDeps)
            context.close()
        then:
            BeanFinalizationException e = thrown(BeanFinalizationException)
            e.message.startsWith("Could not close bean: LifecycleAnnotatedWithDeps")
    }

    static class LifecycleAnnotated {
        boolean initialized = false
        boolean initialized2 = false
        boolean finalized = false
        boolean finalized2 = false

        @Init
        void init() {
            initialized = true
        }

        @Init
        private void init2() {
            initialized2 = true
        }

        @Close
        void close() {
            finalized = true
        }

        @Close
        private void close2() {
            finalized2 = true
        }
    }

    static class LifecycleImplemented implements Initializable, Closeable {
        boolean initialized = false
        boolean finalized = false

        @Override
        void init() {
            initialized = true
        }

        @Override
        void close() {
            finalized = true
        }
    }

    static class LifecycleAnnotatedWithDeps {
        boolean initialized = false
        boolean finalized = false
        LifecycleAnnotated initDependency;
        LifecycleImplemented finalizeDependency;

        @Init
        void init(LifecycleAnnotated lifecycleAnnotated) {
            initialized = true
            initDependency = lifecycleAnnotated
        }

        @Close
        void close(LifecycleImplemented lifecycleImplemented) {
            finalized = true
            finalizeDependency = lifecycleImplemented
        }
    }
}
