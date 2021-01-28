package annotated.samples.condition_mixed


import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.ConditionalOnBean
import com.coditory.quark.context.annotations.ConditionalOnClass

@Bean
@ConditionalOnClass(["java.lang.Integer"])
@ConditionalOnBean([Foo, Bar])
class Baz {
}
