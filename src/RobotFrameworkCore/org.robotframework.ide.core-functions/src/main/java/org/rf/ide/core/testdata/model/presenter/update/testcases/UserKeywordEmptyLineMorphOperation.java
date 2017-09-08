/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UserKeywordEmptyLineMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_EMPTY_LINE;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_EMPTY_LINE;
    }

    @Override
    public AModelElement<?> create(final TestCase testCase, final int index, final String actionName,
            final List<String> args, final String comment) {
        final RobotEmptyRow<TestCase> row = new RobotEmptyRow<>();
        row.setParent(testCase);
        return row;
    }

    @Override
    public RobotEmptyRow<TestCase> insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        @SuppressWarnings("unchecked")
        final RobotEmptyRow<TestCase> emptyLine = (RobotEmptyRow<TestCase>) modelElement;
        emptyLine.setParent(testCase);
        testCase.addElement(emptyLine, index);

        return emptyLine;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        // Do nothing
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        // Do nothing
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final TestCase testCase, final AModelElement<?> modelElement) {
        testCase.removeElement((AModelElement<TestCase>) modelElement);
    }
}
