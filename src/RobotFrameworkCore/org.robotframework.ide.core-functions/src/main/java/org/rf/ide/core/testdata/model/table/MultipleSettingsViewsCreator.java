/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MultipleSettingsViewsCreator {

    public static <T> Optional<T> createView(final List<T> settings, final Function<List<T>, T> viewCreator) {
        if (settings.isEmpty()) {
            return Optional.empty();
        } else if (settings.size() == 1) {
            return Optional.of(settings.get(0));
        } else {
            return Optional.of(viewCreator.apply(settings));
        }
    }
}
