/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;

public class SetCaseSettingArgumentCommand extends SetKeywordCallArgumentCommand {

    public SetCaseSettingArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        super(keywordCall, index, value);
    }

    @Override
    protected void updateModelElement() {
        @SuppressWarnings("unchecked")
        final AModelElement<TestCase> linkedElement = (AModelElement<TestCase>) getKeywordCall().getLinkedElement();
        new TestCaseTableModelUpdater().update(linkedElement, getIndex(), getValue());
    }
}
