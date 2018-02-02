/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.execution.creation;

import static org.assertj.core.api.Assertions.assertThat;

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

public abstract class ACreationOfExecutionRowTest extends RobotFormatParameterizedTest {

    public ACreationOfExecutionRowTest(final String extension, final FileFormat format) {
        super(extension, format);
    }

    public abstract IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> getExecutableWithName();

    public abstract IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> getExecutableWithoutName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName();

    @Test
    public void test_exec_actionOnly() throws Exception {
        assert_execAction_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_actionOnly_unitWithoutName() throws Exception {
        assert_execAction_template(getExecutableWithoutName(), getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execAction_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getActionOnlyCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_commentOnly() throws Exception {
        assert_execComment_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_commentOnly_unitWithoutName() throws Exception {
        assert_execComment_template(getExecutableWithoutName(), getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addCommentPart(cm1);
        robotExecutableRow.addCommentPart(cm2);
        robotExecutableRow.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getCommentOnlyCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_action_withCommentOnly() throws Exception {
        assert_execAction_andComment_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_action_withCommentOnly_unitWithoutName() throws Exception {
        assert_execAction_andComment_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execAction_andComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addCommentPart(cm1);
        robotExecutableRow.addCommentPart(cm2);
        robotExecutableRow.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getActionWithCommentCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_action_withOneArgOnly() throws Exception {
        assert_execAction_andOneArg_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_action_withOneArgOnly_unitWithoutName() throws Exception {
        assert_execAction_andOneArg_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execAction_andOneArg_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        robotExecutableRow.addArgument(arg1);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getActionWithOneArgCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_action_withOneArg_andComment() throws Exception {
        assert_execAction_andOneArg_andComment_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_action_withOneArg_andComment_unitWithoutName() throws Exception {
        assert_execAction_andOneArg_andComment_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execAction_andOneArg_andComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        robotExecutableRow.addArgument(arg1);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addCommentPart(cm1);
        robotExecutableRow.addCommentPart(cm2);
        robotExecutableRow.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getActionWithOneArgAndCommentCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_action_withThreeArgsOnly() throws Exception {
        assert_execAction_andThreeArgs_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_action_withThreeArgsOnly_unitWithoutName() throws Exception {
        assert_execAction_andThreeArgs_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execAction_andThreeArgs_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        robotExecutableRow.addArgument(arg1);
        robotExecutableRow.addArgument(arg2);
        robotExecutableRow.addArgument(arg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getActionWithThreeArgCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_action_withThreeArgs_andComment() throws Exception {
        assert_execAction_andThreeArgs_andComment_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_action_withThreeArgs_andComment_unitWithoutName() throws Exception {
        assert_execAction_andThreeArgs_andComment_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execAction_andThreeArgs_andComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        final RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        final RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        final RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        final RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        robotExecutableRow.addArgument(arg1);
        robotExecutableRow.addArgument(arg2);
        robotExecutableRow.addArgument(arg3);

        final RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        final RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        final RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addCommentPart(cm1);
        robotExecutableRow.addCommentPart(cm2);
        robotExecutableRow.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getActionWithThreeArgAndCommentCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    @Test
    public void test_exec_forEmptyLine() throws Exception {
        assert_execEmptyLine_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName());
    }

    @Test
    public void test_exec_forEmptyLine_unitWithoutName() throws Exception {
        assert_execEmptyLine_template(getExecutableWithoutName(), getCompareFilesStoreForExecutableWithoutName());
    }

    private void assert_execEmptyLine_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);
        assertThat(robotExecutableRow).isNotNull();

        // test data prepare
        // nothing to do

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileStore.getEmptyLineCmpFile(),
                execUnit.getHolder().getParent().getParent());
    }

    private RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> getExecUnitToModify(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit) {
        final List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        final RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = executionContext
                .get(executionContext.size() - 1);
        return robotExecutableRow;
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

        private String actionOnlyCmpFile;

        private String commentOnlyCmpFile;

        private String actionWithCommentCmpFile;

        private String actionWithOneArgCmpFile;

        private String actionWithOneArgAndCommentCmpFile;

        private String actionWithThreeArgCmpFile;

        private String actionWithThreeArgAndCommentCmpFile;

        private String emptyLineCmpFile;

        @ValidateNotNull(errorParameterMsg = "action only")
        public String getActionOnlyCmpFile() {
            return actionOnlyCmpFile;
        }

        public void setActionOnlyCmpFile(final String actionOnlyCmpFile) {
            this.actionOnlyCmpFile = actionOnlyCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "comment only")
        public String getCommentOnlyCmpFile() {
            return commentOnlyCmpFile;
        }

        public void setCommentOnlyCmpFile(final String commentOnlyCmpFile) {
            this.commentOnlyCmpFile = commentOnlyCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with comment")
        public String getActionWithCommentCmpFile() {
            return actionWithCommentCmpFile;
        }

        public void setActionWithCommentCmpFile(final String actionWithCommentCmpFile) {
            this.actionWithCommentCmpFile = actionWithCommentCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with one arg")
        public String getActionWithOneArgCmpFile() {
            return actionWithOneArgCmpFile;
        }

        public void setActionWithOneArgCmpFile(final String actionWithOneArgCmpFile) {
            this.actionWithOneArgCmpFile = actionWithOneArgCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with one arg and comment")
        public String getActionWithOneArgAndCommentCmpFile() {
            return actionWithOneArgAndCommentCmpFile;
        }

        public void setActionWithOneArgAndCommentCmpFile(final String actionWithOneArgAndCommentCmpFile) {
            this.actionWithOneArgAndCommentCmpFile = actionWithOneArgAndCommentCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with three args")
        public String getActionWithThreeArgCmpFile() {
            return actionWithThreeArgCmpFile;
        }

        public void setActionWithThreeArgCmpFile(final String actionWithThreeArgCmpFile) {
            this.actionWithThreeArgCmpFile = actionWithThreeArgCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with three args and comment")
        public String getActionWithThreeArgAndCommentCmpFile() {
            return actionWithThreeArgAndCommentCmpFile;
        }

        public void setActionWithThreeArgAndCommentCmpFile(final String actionWithThreeArgAndCommentCmpFile) {
            this.actionWithThreeArgAndCommentCmpFile = actionWithThreeArgAndCommentCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "empty line")
        public String getEmptyLineCmpFile() {
            return emptyLineCmpFile;
        }

        public void setEmptyLineCmpFile(final String emptyLineCmpFile) {
            this.emptyLineCmpFile = emptyLineCmpFile;
        }
    }
}
