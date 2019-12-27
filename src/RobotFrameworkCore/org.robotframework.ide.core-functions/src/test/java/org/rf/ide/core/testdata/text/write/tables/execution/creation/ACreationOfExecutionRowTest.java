/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.execution.creation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FileFormat;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ATestFilesCompareStore.InvalidTestStoreException;

public abstract class ACreationOfExecutionRowTest {

    public abstract IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> getExecutableWithName();

    public abstract IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> getExecutableWithoutName();

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithName(FileFormat format);

    public abstract TestFilesCompareStore getCompareFilesStoreForExecutableWithoutName(FileFormat format);

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_actionOnly(final FileFormat format) throws Exception {
        assert_execAction_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_actionOnly_unitWithoutName(final FileFormat format) throws Exception {
        assert_execAction_template(getExecutableWithoutName(), getCompareFilesStoreForExecutableWithoutName(format),
                format);
    }

    private void assert_execAction_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_commentOnly(final FileFormat format) throws Exception {
        assert_execComment_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_commentOnly_unitWithoutName(final FileFormat format) throws Exception {
        assert_execComment_template(getExecutableWithoutName(), getCompareFilesStoreForExecutableWithoutName(format),
                format);
    }

    private void assert_execComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withCommentOnly(final FileFormat format) throws Exception {
        assert_execAction_andComment_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withCommentOnly_unitWithoutName(final FileFormat format) throws Exception {
        assert_execAction_andComment_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName(format), format);
    }

    private void assert_execAction_andComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withOneArgOnly(final FileFormat format) throws Exception {
        assert_execAction_andOneArg_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withOneArgOnly_unitWithoutName(final FileFormat format) throws Exception {
        assert_execAction_andOneArg_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName(format), format);
    }

    private void assert_execAction_andOneArg_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withOneArg_andComment(final FileFormat format) throws Exception {
        assert_execAction_andOneArg_andComment_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withOneArg_andComment_unitWithoutName(final FileFormat format) throws Exception {
        assert_execAction_andOneArg_andComment_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName(format), format);
    }

    private void assert_execAction_andOneArg_andComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withThreeArgsOnly(final FileFormat format) throws Exception {
        assert_execAction_andThreeArgs_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withThreeArgsOnly_unitWithoutName(final FileFormat format) throws Exception {
        assert_execAction_andThreeArgs_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName(format), format);
    }

    private void assert_execAction_andThreeArgs_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withThreeArgs_andComment(final FileFormat format) throws Exception {
        assert_execAction_andThreeArgs_andComment_template(getExecutableWithName(),
                getCompareFilesStoreForExecutableWithName(format), format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_action_withThreeArgs_andComment_unitWithoutName(final FileFormat format) throws Exception {
        assert_execAction_andThreeArgs_andComment_template(getExecutableWithoutName(),
                getCompareFilesStoreForExecutableWithoutName(format), format);
    }

    private void assert_execAction_andThreeArgs_andComment_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_forEmptyLine(final FileFormat format) throws Exception {
        assert_execEmptyLine_template(getExecutableWithName(), getCompareFilesStoreForExecutableWithName(format),
                format);
    }

    @ParameterizedTest
    @EnumSource(value = FileFormat.class, names = { "TXT_OR_ROBOT", "TSV" })
    public void test_exec_forEmptyLine_unitWithoutName(final FileFormat format) throws Exception {
        assert_execEmptyLine_template(getExecutableWithoutName(), getCompareFilesStoreForExecutableWithoutName(format),
                format);
    }

    private void assert_execEmptyLine_template(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final TestFilesCompareStore fileStore, final FileFormat format) throws Exception {
        // prepare
        checkEnvironment(format);
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

    private void checkEnvironment(final FileFormat format) throws InvalidTestStoreException {
        final TestFilesCompareStore cmpExecWithName = getCompareFilesStoreForExecutableWithName(format);
        if (!cmpExecWithName.wasValidated()) {
            cmpExecWithName.validate();
        }

        final TestFilesCompareStore cmpExecWithoutName = getCompareFilesStoreForExecutableWithoutName(format);
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
