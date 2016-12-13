package org.robotframework.red.junit;

import org.assertj.core.api.Condition;

import com.google.common.base.Optional;

public class Conditions {

    public static <T> Condition<Optional<? extends T>> present() {
        return new Condition<Optional<? extends T>>("present") {
            @Override
            public boolean matches(final Optional<? extends T> optional) {
                return optional.isPresent();
            }
        };
    }

    public static <T> Condition<Optional<? extends T>> containing(final T element) {
        return new Condition<Optional<? extends T>>("present with '" + element.toString() + "'inside") {
            @Override
            public boolean matches(final Optional<? extends T> optional) {
                if (optional.isPresent()) {
                    return optional.get().equals(element);
                }
                return false;
            }
        };
    }

    public static <T> Condition<Optional<? extends T>> absent() {
        return new Condition<Optional<? extends T>>("absent") {
            @Override
            public boolean matches(final Optional<? extends T> optional) {
                return !optional.isPresent();
            }
        };
    }
}
