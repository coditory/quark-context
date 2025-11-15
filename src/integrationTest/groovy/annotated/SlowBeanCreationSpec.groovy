package annotated

import ch.qos.logback.classic.Logger
import com.coditory.quark.context.Context
import com.coditory.quark.context.base.CapturingAppender
import org.slf4j.LoggerFactory
import spock.lang.Specification

class SlowBeanCreationSpec extends Specification {
    CapturingAppender appender = new CapturingAppender()

    void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(appender)
    }

    void cleanup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.detachAppender(appender)
    }

    def "should log slow bean creation"() {
        given:
            Context context = Context.scanPackage(annotated.samples.slow_beans.Foo)
        when:
            context.get(annotated.samples.slow_beans.Foo)

        then:
            List<String> logs = appender.getLogsByMessagePrefix("Slow bean creation")
            logs.size() == 2
            logs.get(0).startsWith("[WARN] Slow bean creation. Bean: Bar")
            logs.get(1).startsWith("[WARN] Slow bean creation. Bean: Foo")
    }

    def "should skip logging slow bean creation with custom threshold"() {
        given:
            Context context = Context.scanPackage(annotated.samples.slow_beans_annotated.Foo)
        when:
            context.get(annotated.samples.slow_beans_annotated.Foo)

        then:
            List<String> logs = appender.getLogsByMessagePrefix("Slow bean creation")
            logs.size() == 1
            logs.get(0).startsWith("[WARN] Slow bean creation. Bean: Bar")
    }
}
