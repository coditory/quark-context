package annotated.samples.beans_circular_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull;

@Bean
class BarBar {
    final BarBar barBar

    BarBar(BarBar barBar) {
        this.barBar = requireNonNull(barBar)
    }
}
