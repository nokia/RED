/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.AExecutableTableElementDumper;
import org.rf.ide.core.testdata.text.write.tables.ForContinueStartWithCommentFixer;

public class TestCaseEmptyLineDumper extends AExecutableTableElementDumper {

    public TestCaseEmptyLineDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.TEST_CASE_EMPTY_LINE);
        addAfterSortTask(new ForContinueStartWithCommentFixer());
    }

    @SuppressWarnings("unchecked")
    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        final RobotEmptyRow<TestCase> testCaseEmptyLine = (RobotEmptyRow<TestCase>) currentElement;
        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();

        final List<RobotToken> keys = new ArrayList<>();
        if (testCaseEmptyLine.getDeclaration() != null) {
            keys.add(testCaseEmptyLine.getDeclaration());
        }

        return sorter;
    }

    @Override
    public boolean isServedType(final AModelElement<? extends IExecutableStepsHolder<?>> element) {
        return super.isServedType(element);
    }
}
