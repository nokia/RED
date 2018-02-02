/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.execution.creation;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ATestFilesCompareStore.InvalidTestStoreException;

public abstract class ACreationOfThreeExecUnitsTest extends RobotFormatParameterizedTest {

    public ACreationOfThreeExecUnitsTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    public abstract List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesAllWithNames();

    public abstract List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> getExecutablesTheFirstWithoutName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithTheFirstWithoutName();

    @Test
    public void test_exec_actions_withoutCommentedLine() throws Exception {
        assert_three_execUnits_noCommentedLines_template(getExecutablesAllWithNames(),
                getCompareFilesStoreForExecutableWithName());

    }

    @Test
    public void test_exec_actions_withoutCommentedLine_unitWithoutName() throws Exception {
        assert_three_execUnits_noCommentedLines_template(getExecutablesTheFirstWithoutName(),
                getCompareFilesStoreForExecutableWithTheFirstWithoutName());
    }

    private void assert_three_execUnits_noCommentedLines_template(
            final List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>> execUnitOneExecRowOne = execUnit
                .get(0).getExecutionContext().get(0);
        final RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>> execUnitTwoExecRowOne = execUnit
                .get(1).getExecutionContext().get(0);
        final RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>> execUnitThreeExecRowOne = execUnit
                .get(2).getExecutionContext().get(0);

        // test data prepare
        //// exec unit 1
        ///// line 1
        final RobotToken action1 = new RobotToken();
        action1.setText("execAction1");
        execUnitOneExecRowOne.setAction(action1);

        //// exec unit 2
        ///// line 1
        final RobotToken action2 = new RobotToken();
        action2.setText("execAction2");
        execUnitTwoExecRowOne.setAction(action2);

        final RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        final RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        final RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execUnitTwoExecRowOne.addArgument(arg1_2);
        execUnitTwoExecRowOne.addArgument(arg2_2);
        execUnitTwoExecRowOne.addArgument(arg3_2);

        //// exec unit 3
        ///// line 1
        final RobotToken action3 = new RobotToken();
        action3.setText("execAction3");
        execUnitThreeExecRowOne.setAction(action3);

        final RobotToken arg1_3 = new RobotToken();
        arg1_3.setText("arg1a");

        execUnitThreeExecRowOne.addArgument(arg1_3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execUnitThreeExecRowOne.addCommentPart(cm1);
        execUnitThreeExecRowOne.addCommentPart(cm2);
        execUnitThreeExecRowOne.addCommentPart(cm3);
    }

    private void checkEnvironment() throws InvalidTestStoreException {
        final TestFilesCompareStore cmpExecWithName = getCompareFilesStoreForExecutableWithName();
        if (!cmpExecWithName.wasValidated()) {
            cmpExecWithName.validate();
        }

        final TestFilesCompareStore cmpExecWithoutName = getCompareFilesStoreForExecutableWithTheFirstWithoutName();
        if (!cmpExecWithoutName.wasValidated()) {
            cmpExecWithoutName.validate();
        }
    }

    public static class TestFilesCompareStore extends ATestFilesCompareStore {

        private String threeExecUnitsWithOneLineEachOtherInsideCmpFile;

        @ValidateNotNull(errorParameterMsg = "three exec units with one line inside")
        public String getThreeExecUnitsWithOneLineEachOtherInsideCmpFile() {
            return threeExecUnitsWithOneLineEachOtherInsideCmpFile;
        }

        public void setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                final String threeExecUnitsWithOneLineEachOtherInsideCmpFile) {
            this.threeExecUnitsWithOneLineEachOtherInsideCmpFile = threeExecUnitsWithOneLineEachOtherInsideCmpFile;
        }
    }
}
