/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;

public class SettingWithDocumentationInput extends InternalElementInput<RobotSetting> {

    public SettingWithDocumentationInput(final RobotSetting docSetting) {
        super(docSetting);
    }

    @Override
    protected String createHeader() {
        return SuiteFileInput.suiteHeader(element.getSuiteFile());
    }

    @Override
    protected Documentation createDocumentation() {
        return element.getSuiteFile().createDocumentation();
    }
}
