/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.eclipse.swt.widgets.Shell;

class Conditions {

    static Condition<? super Shell[]> shellWithText(final String text) {
        return new Condition<>(shells -> Stream.of(shells).anyMatch(s -> s.getText().equals(text)),
                "Shell with text '" + text + "'");
    }
}
