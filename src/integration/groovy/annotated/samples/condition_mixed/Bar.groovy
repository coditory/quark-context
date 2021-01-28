package annotated.samples.condition_mixed


import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnBean

@Bean
@ConditionalOnBean([Foo])
class Bar {
}
