/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotVersion;

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

    public static <P, T extends AModelElement<P>> List<T> createView(final RobotVersion version, final P parent,
            final List<T> settings, final Function<List<T>, T> viewCreator) {
        if (version.isOlderThan(new RobotVersion(3, 0))) {
            final List<T> tmpSettings = new ArrayList<>();
            final Optional<T> view = createView(settings, viewCreator);
            view.ifPresent(v -> v.setParent(parent));
            view.ifPresent(tmpSettings::add);
            return Collections.unmodifiableList(tmpSettings);
        } else {
            return Collections.unmodifiableList(settings);
        }
    }
}
