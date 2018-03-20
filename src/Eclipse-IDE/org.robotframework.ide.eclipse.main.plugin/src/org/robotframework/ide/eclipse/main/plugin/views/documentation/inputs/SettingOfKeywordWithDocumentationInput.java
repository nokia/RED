/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

public class SettingOfKeywordWithDocumentationInput extends InternalElementInput<RobotDefinitionSetting> {

    public SettingOfKeywordWithDocumentationInput(final RobotDefinitionSetting docSetting) {
        super(docSetting);
    }

    @Override
    protected String createHeader() {
        return KeywordDefinitionInput.keywordHeader((RobotKeywordDefinition) element.getParent());
    }

    @Override
    protected Documentation createDocumentation() {
        return ((RobotKeywordDefinition) element.getParent()).createDocumentation();
    }

}
