/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsColumnsPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsDataProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowView;

class CasesDataProvider extends CodeElementsDataProvider<RobotCasesSection> {

    CasesDataProvider(final CodeElementsColumnsPropertyAccessor propertyAccessor, final RobotCasesSection section) {
        super(section, propertyAccessor, CasesAdderState.CASE, CasesAdderState.CALL);
    }

    @Override
    protected boolean shouldAddSetting(final RobotKeywordCall setting) {
        @SuppressWarnings("unchecked")
        final AModelElement<TestCase> linkedSetting = (AModelElement<TestCase>) setting.getLinkedElement();
        final TestCase testCase = linkedSetting.getParent();
        return !testCase.isDuplicatedSetting(linkedSetting);
    }

    @Override
    protected int numberOfColumns(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;

            if (!call.isDocumentationSetting()) {
                return ExecutablesRowView.rowTokens((RobotKeywordCall) element).size() + 1;
            }
        }
        return 0;
    }
}
