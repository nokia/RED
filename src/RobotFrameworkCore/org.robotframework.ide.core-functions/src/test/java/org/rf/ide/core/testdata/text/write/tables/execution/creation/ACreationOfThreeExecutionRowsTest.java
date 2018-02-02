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
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.RobotFormatParameterizedTest;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ATestFilesCompareStore.InvalidTestStoreException;

public abstract class ACreationOfThreeExecutionRowsTest extends RobotFormatParameterizedTest {

    public ACreationOfThreeExecutionRowsTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    public abstract IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> getExecutableWithName();

    public abstract IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> getExecutableWithoutName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName();

    @Test
    public void test_exec_actions_withoutCommentedLine() throws Exception {
        assert_three_execActions_combination_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_actions_withoutCommentedLine_unitWithoutName() throws Exception {
        assert_three_execActions_combination_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_three_execActions_combination_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execOne = executionContext.get(0);
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execTwo = executionContext.get(1);
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execThree = executionContext.get(2);

        // test data prepare
        //// line 1
        final RobotToken action1 = new RobotToken();
        action1.setText("execAction1");
        execOne.setAction(action1);

        //// line 2
        final RobotToken action2 = new RobotToken();
        action2.setText("execAction2");
        execTwo.setAction(action2);

        final RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        final RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        final RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execTwo.addArgument(arg1_2);
        execTwo.addArgument(arg2_2);
        execTwo.addArgument(arg3_2);

        //// line 3
        final RobotToken action3 = new RobotToken();
        action3.setText("execAction3");
        execThree.setAction(action3);

        final RobotToken arg1_3 = new RobotToken();
        arg1_3.setText("arg1a");

        execThree.addArgument(arg1_3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execThree.addCommentPart(cm1);
        execThree.addCommentPart(cm2);
        execThree.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getThreeLinesWithoutCommentedLineCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_actions_withCommentedAndEmptyLine() throws Exception {
        assert_three_execActions_withCommented_andEmptyLine_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_actions_withCommentedAndEmptyLine_unitWithoutName() throws Exception {
        assert_three_execActions_withCommented_andEmptyLine_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_three_execActions_withCommented_andEmptyLine_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execOne = executionContext.get(0);
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execTwo = executionContext.get(1);

        // test data prepare
        //// line 1

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execOne.addCommentPart(cm1);
        execOne.addCommentPart(cm2);
        execOne.addCommentPart(cm3);

        //// line 2
        final RobotToken action2 = new RobotToken();
        action2.setText("execAction1");
        execTwo.setAction(action2);

        final RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        final RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        final RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execTwo.addArgument(arg1_2);
        execTwo.addArgument(arg2_2);
        execTwo.addArgument(arg3_2);

        final RobotToken cm1_2 = new RobotToken();
        cm1_2.setText("cm1a");
        final RobotToken cm2_2 = new RobotToken();
        cm2_2.setText("cm2a");
        final RobotToken cm3_2 = new RobotToken();
        cm3_2.setText("cm3a");

        execTwo.addCommentPart(cm1_2);
        execTwo.addCommentPart(cm2_2);
        execTwo.addCommentPart(cm3_2);

        //// line 3
        // just empty

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getThreeLinesWithCommentAndEmptyLineCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_actions_withCommentedAndEmptyLineInMiddle() throws Exception {
        assert_three_execActions_withComment_andEmptyLineInMiddle_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_actions_withCommentedAndEmptyLineInMiddle_unitWithoutName() throws Exception {
        assert_three_execActions_withComment_andEmptyLineInMiddle_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_three_execActions_withComment_andEmptyLineInMiddle_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execOne = executionContext.get(0);
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execThree = executionContext.get(2);

        // test data prepare
        //// line 1

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execOne.addCommentPart(cm1);
        execOne.addCommentPart(cm2);
        execOne.addCommentPart(cm3);

        //// line 2
        // just empty

        //// line 3
        final RobotToken action2 = new RobotToken();
        action2.setText("execAction1");
        execThree.setAction(action2);

        final RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        final RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        final RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execThree.addArgument(arg1_2);
        execThree.addArgument(arg2_2);
        execThree.addArgument(arg3_2);

        final RobotToken cm1_2 = new RobotToken();
        cm1_2.setText("cm1a");
        final RobotToken cm2_2 = new RobotToken();
        cm2_2.setText("cm2a");
        final RobotToken cm3_2 = new RobotToken();
        cm3_2.setText("cm3a");

        execThree.addCommentPart(cm1_2);
        execThree.addCommentPart(cm2_2);
        execThree.addCommentPart(cm3_2);
        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(
                fileStore.getThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    private void checkEnvironment() throws InvalidTestStoreException {
        final TestFilesCompareStore cmpExecWithName = getCompareFilesStoreForExecutableWithName();
        if (!cmpExecWithName.wasValidated()) {
            cmpExecWithName.validate();
        }

        final TestFilesCompareStore cmpExecWithoutName = getCompareFilesStoreForExecutableWithoutName();
        if (!cmpExecWithoutName.wasValidated()) {
            cmpExecWithoutName.validate();
        }
    }

    public static class TestFilesCompareStore extends ATestFilesCompareStore {

        private String threeLinesWithoutCommentedLineCmpFile;

        private String threeLinesWithCommentAndEmptyLineCmpFile;

        private String threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile;

        @ValidateNotNull(errorParameterMsg = "three lines with comment as first and empty line in the middle")
        public String getThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile() {
            return threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile;
        }

        public void setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                final String threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile) {
            this.threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile = threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "three lines with commented and empty line")
        public String getThreeLinesWithCommentAndEmptyLineCmpFile() {
            return threeLinesWithCommentAndEmptyLineCmpFile;
        }

        public void setThreeLinesWithCommentAndEmptyLineCmpFile(final String threeLinesWithCommentAndEmptyLineCmpFile) {
            this.threeLinesWithCommentAndEmptyLineCmpFile = threeLinesWithCommentAndEmptyLineCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "three lines without commented any of them")
        public String getThreeLinesWithoutCommentedLineCmpFile() {
            return threeLinesWithoutCommentedLineCmpFile;
        }

        public void setThreeLinesWithoutCommentedLineCmpFile(final String threeLinesWithoutCommentedLineCmpFile) {
            this.threeLinesWithoutCommentedLineCmpFile = threeLinesWithoutCommentedLineCmpFile;
        }
    }
}
