/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class RobotPreferencesPage extends RedPreferencePage {

    public RobotPreferencesPage() {
        super("Main RED preference page");
    }

    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        return null;
    }
}
