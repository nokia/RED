/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;

public class SettingOfCaseWithDocumentationInput extends InternalElementInput<RobotDefinitionSetting> {

    public SettingOfCaseWithDocumentationInput(final RobotDefinitionSetting docSetting) {
        super(docSetting);
    }

    @Override
    protected String createHeader() {
        return TestCaseInput.testHeader((RobotCase) element.getParent());
    }

    @Override
    protected Documentation createDocumentation() {
        return ((RobotCase) element.getParent()).createDocumentation();
    }

}
