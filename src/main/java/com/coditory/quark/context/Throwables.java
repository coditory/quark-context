package com.coditory.quark.context;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coditory.quark.context.Args.checkNonNull;

final class Throwables {
    private Throwables() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T getRootCauseOfType(Throwable throwable, Class<T> type) {
        checkNonNull(throwable, "throwable");
        checkNonNull(type, "type");
        List<Throwable> list = getCauses(throwable);
        Collections.reverse(list);
        return list.stream()
                .filter(type::isInstance)
                .map(e -> (T) e)
                .findFirst()
                .orElse(null);
    }

    private static List<Throwable> getCauses(Throwable throwable) {
        checkNonNull(throwable, "throwable");
        List<Throwable> list = new ArrayList<>();
        Set<Throwable> visited = new HashSet<>();
        while (throwable != null && !visited.contains(throwable)) {
            list.add(throwable);
            visited.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }
}
