package annotated.samples.beans.bar

import com.coditory.quark.context.annotations.Bean
import groovy.transform.PackageScope

@Bean
class Bar {
    @PackageScope
    Bar() {
    }
}
