package annotated.samples.condition_on_property

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnDisabledProperty

@Bean
@ConditionalOnDisabledProperty("foo.enabled")
class Bar {
}
