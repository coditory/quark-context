package annotated.samples.condition_on_missing_bean

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnMissingBean

@Bean
@ConditionalOnMissingBean([Baz])
class Bar {
}
