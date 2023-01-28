package annotated.samples.condition_on_class

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnClass

@Bean
@ConditionalOnClass("java.lang.Integer")
class Foo {
}
