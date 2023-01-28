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

    def "should log slow bean initialization"() {
        given:
            Context context = Context.scanPackage(annotated.samples.slow_beans.Bar)
        when:
            annotated.samples.slow_beans.Baz baz = context.get(annotated.samples.slow_beans.Baz)

        then:
            List<String> logs = appender.getLogsByMessagePrefix("Slow bean creation")
            logs.size() == 1
            logs.first().startsWith("[WARN] Slow bean creation. Bean: Bar")
    }
}
