package annotated.samples.condition_circular

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnMissingBean

@Bean
@ConditionalOnMissingBean([Bar])
class Foo {
}
