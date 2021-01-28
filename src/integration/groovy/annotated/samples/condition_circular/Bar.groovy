package annotated.samples.condition_circular

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnBean

@Bean
@ConditionalOnBean([Foo])
class Bar {
}
