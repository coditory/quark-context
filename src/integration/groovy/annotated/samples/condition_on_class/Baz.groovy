package annotated.samples.condition_on_class

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnBean

@Bean
@ConditionalOnBean([Foo])
class Baz {
}
