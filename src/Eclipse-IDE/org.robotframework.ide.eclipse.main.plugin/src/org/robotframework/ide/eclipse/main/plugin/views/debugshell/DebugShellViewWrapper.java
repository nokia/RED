/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import org.eclipse.e4.tools.compat.parts.DIViewPart;


@SuppressWarnings("restriction")
public class DebugShellViewWrapper extends DIViewPart<DebugShellView> {

    public DebugShellViewWrapper() {
        super(DebugShellView.class);
    }

    public DebugShellView getView() {
        return getComponent();
    }
}
