/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class Controls {

    public static Optional<Control> findControlSatisfying(final Composite parent, final Predicate<Control> predicate) {
        return getControls(parent).stream().filter(predicate).findFirst();
    }

    public static Stream<Control> getControlsStream(final Control parent) {
        return getControls(parent).stream();
    }

    public static List<Control> getControls(final Control parent) {
        final List<Control> controls = new ArrayList<>();
        getControls(controls, parent);
        return controls;
    }

    public static <C extends Control> List<C> getControls(final Control parent, final Class<C> controlClass) {
        return getControlsStream(parent, controlClass).collect(toList());
    }

    public static <C extends Control> Stream<C> getControlsStream(final Control parent, final Class<C> controlClass) {
        return getControlsStream(parent).filter(controlClass::isInstance).map(controlClass::cast);
    }

    private static void getControls(final List<Control> controls, final Control parent) {
        if (parent instanceof Composite) {
            if (parent.getClass() != Composite.class && parent.getClass() != Shell.class) {
                controls.add(parent);
            }
            final Composite composite = (Composite) parent;
            for (final Control child : composite.getChildren()) {
                getControls(controls, child);
            }
        } else {
            controls.add(parent);
        }
    }
}
