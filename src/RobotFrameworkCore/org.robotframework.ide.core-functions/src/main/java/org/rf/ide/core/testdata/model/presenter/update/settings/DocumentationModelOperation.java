/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DocumentationModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_DOCUMENTATION_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.SUITE_DOCUMENTATION);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final List<String> args, final String comment) {
        final SuiteDocumentation newSuiteDocumentation = settingsTable.newSuiteDocumentation();
        for (int i = 0; i < args.size(); i++) {
            newSuiteDocumentation.addDocumentationText(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newSuiteDocumentation.setComment(comment);
        }
        return newSuiteDocumentation;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final SuiteDocumentation suiteDocumentation = (SuiteDocumentation) modelElement;
        if (index >= 0) {
            suiteDocumentation.setDocumentationText(index, value);
        }
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeDocumentation();
    }
}
