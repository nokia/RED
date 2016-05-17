/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.testcases.creation;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecUnitsTest;

public abstract class ACreationOfThreeTestCaseExecutablesWithOneStepTest extends ACreationOfThreeExecUnitsTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "testCases//exec//new//threeTestCases//oneExecInEveryTestCase//";

    private final String extension;

    public ACreationOfThreeTestCaseExecutablesWithOneStepTest(final String extension) {
        this.extension = extension;
    }

    @Override
    public List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesAllWithNames() {
        List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> units = createModelWithOneTestCaseInside();
        for (int index = 0; index < 3; index++) {
            String name = "TestCase";
            if (index > 0) {
                name += (index + 1);
            }

            ((TestCase) units.get(index).getHolder()).getTestName().setText(name);
        }

        return units;
    }

    @Override
    public List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesTheFirstWithoutName() {
        List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> units = createModelWithOneTestCaseInside();
        for (int index = 1; index < 3; index++) {
            String name = "TestCase";
            name += (index + 1);
            ((TestCase) units.get(index).getHolder()).getTestName().setText(name);
        }

        return units;
    }

    private List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> createModelWithOneTestCaseInside() {
        List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> execUnits = new ArrayList<>(
                0);

        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");
        modelFile.includeTestCaseTableSection();
        TestCaseTable keywordTable = modelFile.getTestCaseTable();

        for (int index = 0; index < 3; index++) {
            RobotToken testName = new RobotToken();
            TestCase execUnit = new TestCase(testName);
            execUnit.addTestExecutionRow(new RobotExecutableRow<TestCase>());
            keywordTable.addTest(execUnit);
            execUnits.add(execUnit);
        }

        return execUnits;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                convert("TestExecutionActionWithAllCombinationsNoCommentLine"));

        return store;
    }

    @Override
    public TestFilesCompareStore getCompareFilesStoreForExecutableWithTheFirstWithoutName() {
        final TestFilesCompareStore store = new TestFilesCompareStore();

        store.setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                convert("TestExecutionActionWithAllCombinationsNoCommentLineWithoutOneTestName"));

        return store;
    }

    public String convert(final String fileName) {
        return PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
    }

    public String getExtension() {
        return extension;
    }
}
