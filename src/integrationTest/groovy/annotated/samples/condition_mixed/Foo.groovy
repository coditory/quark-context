package annotated.samples.condition_mixed

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnProperty

@Bean
@ConditionalOnProperty("foo.enabled")
class Foo {
}
