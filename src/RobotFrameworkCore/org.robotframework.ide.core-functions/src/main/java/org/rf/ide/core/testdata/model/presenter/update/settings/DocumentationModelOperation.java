/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Joiner;

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
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args,
            final String comment) {
        final SuiteDocumentation newSuiteDocumentation = settingsTable.newSuiteDocumentation();
        DocumentationServiceHandler.update(newSuiteDocumentation, Joiner.on(' ').join(args));

        if (comment != null && !comment.isEmpty()) {
            newSuiteDocumentation.setComment(comment);
        }
        return newSuiteDocumentation;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final SuiteDocumentation suiteDocumentation = (SuiteDocumentation) modelElement;
        DocumentationServiceHandler.update(suiteDocumentation, value);
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeDocumentation();
    }
}
