/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.execution.creation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfExecutionRowTest.TestFilesCompareStore.InvalidTestStoreException;

import com.google.common.base.Joiner;

public abstract class ACreationOfExecutionRowTest {

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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken action = new RobotToken();
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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addComment(cm1);
        robotExecutableRow.addComment(cm2);
        robotExecutableRow.addComment(cm3);

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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addComment(cm1);
        robotExecutableRow.addComment(cm2);
        robotExecutableRow.addComment(cm3);

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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        RobotToken arg1 = new RobotToken();
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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        robotExecutableRow.addArgument(arg1);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addComment(cm1);
        robotExecutableRow.addComment(cm2);
        robotExecutableRow.addComment(cm3);

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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
                execUnit);

        // test data prepare
        RobotToken action = new RobotToken();
        action.setText("execAction");
        robotExecutableRow.setAction(action);

        RobotToken arg1 = new RobotToken();
        arg1.setText("arg1");
        RobotToken arg2 = new RobotToken();
        arg2.setText("arg2");
        RobotToken arg3 = new RobotToken();
        arg3.setText("arg3");

        robotExecutableRow.addArgument(arg1);
        robotExecutableRow.addArgument(arg2);
        robotExecutableRow.addArgument(arg3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        robotExecutableRow.addComment(cm1);
        robotExecutableRow.addComment(cm2);
        robotExecutableRow.addComment(cm3);

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
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = getExecUnitToModify(
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
        List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> robotExecutableRow = executionContext
                .get(executionContext.size() - 1);
        return robotExecutableRow;
    }

    private void checkEnvironment() throws InvalidTestStoreException {
        final TestFilesCompareStore cmpExecWithName = getCompareFilesStoreForExecutableWithName();
        if (!cmpExecWithName.wasValidated.get()) {
            cmpExecWithName.validate();
        }

        final TestFilesCompareStore cmpExecWithoutName = getCompareFilesStoreForExecutableWithoutName();
        if (!cmpExecWithoutName.wasValidated.get()) {
            cmpExecWithoutName.validate();
        }
    }

    public static class TestFilesCompareStore {

        private String actionOnlyCmpFile;

        private String commentOnlyCmpFile;

        private String actionWithCommentCmpFile;

        private String actionWithOneArgCmpFile;

        private String actionWithOneArgAndCommentCmpFile;

        private String actionWithThreeArgCmpFile;

        private String actionWithThreeArgAndCommentCmpFile;

        private String emptyLineCmpFile;

        private AtomicBoolean wasValidated = new AtomicBoolean(false);

        @ValidateNotNull(errorParameterMsg = "action only")
        public String getActionOnlyCmpFile() {
            return actionOnlyCmpFile;
        }

        public void setActionOnlyCmpFile(String actionOnlyCmpFile) {
            this.actionOnlyCmpFile = actionOnlyCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "comment only")
        public String getCommentOnlyCmpFile() {
            return commentOnlyCmpFile;
        }

        public void setCommentOnlyCmpFile(String commentOnlyCmpFile) {
            this.commentOnlyCmpFile = commentOnlyCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with comment")
        public String getActionWithCommentCmpFile() {
            return actionWithCommentCmpFile;
        }

        public void setActionWithCommentCmpFile(String actionWithCommentCmpFile) {
            this.actionWithCommentCmpFile = actionWithCommentCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with one arg")
        public String getActionWithOneArgCmpFile() {
            return actionWithOneArgCmpFile;
        }

        public void setActionWithOneArgCmpFile(String actionWithOneArgCmpFile) {
            this.actionWithOneArgCmpFile = actionWithOneArgCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with one arg and comment")
        public String getActionWithOneArgAndCommentCmpFile() {
            return actionWithOneArgAndCommentCmpFile;
        }

        public void setActionWithOneArgAndCommentCmpFile(String actionWithOneArgAndCommentCmpFile) {
            this.actionWithOneArgAndCommentCmpFile = actionWithOneArgAndCommentCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with three args")
        public String getActionWithThreeArgCmpFile() {
            return actionWithThreeArgCmpFile;
        }

        public void setActionWithThreeArgCmpFile(String actionWithThreeArgCmpFile) {
            this.actionWithThreeArgCmpFile = actionWithThreeArgCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "action with three args and comment")
        public String getActionWithThreeArgAndCommentCmpFile() {
            return actionWithThreeArgAndCommentCmpFile;
        }

        public void setActionWithThreeArgAndCommentCmpFile(String actionWithThreeArgAndCommentCmpFile) {
            this.actionWithThreeArgAndCommentCmpFile = actionWithThreeArgAndCommentCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "empty line")
        public String getEmptyLineCmpFile() {
            return emptyLineCmpFile;
        }

        public void setEmptyLineCmpFile(String emptyLineCmpFile) {
            this.emptyLineCmpFile = emptyLineCmpFile;
        }

        public void validate() throws InvalidTestStoreException {
            final List<String> errors = new ArrayList<>(0);
            errors.addAll(collectMistmatchesForNotNullValidation());

            this.wasValidated.set(true);
            if (!errors.isEmpty()) {
                throw new InvalidTestStoreException(errors);
            }
        }

        protected List<String> collectMistmatchesForNotNullValidation() {
            final List<String> errors = new ArrayList<>(0);
            final Class<ValidateNotNull> ano = ValidateNotNull.class;
            final List<Method> publicMethodsAnnotatedWith = getPublicMethodsAnnotatedWith(ano);
            for (final Method method : publicMethodsAnnotatedWith) {
                ValidateNotNull validError = method.getAnnotation(ano);
                try {
                    final Object invoke = method.invoke(this);
                    if (invoke == null || ((String) invoke).isEmpty()) {
                        errors.add("Method \'" + method.getName() + "\' should return null for file path "
                                + validError.errorParameterMsg());
                    }
                } catch (final Exception e) {
                    errors.add("Problem found when \'" + validError.errorParameterMsg() + "\' with message " + e);
                }
            }

            return errors;
        }

        protected List<Method> getPublicMethodsAnnotatedWith(final Class<? extends Annotation> ano) {
            final List<Method> methodsToCheck = new ArrayList<>(0);

            final Method[] declaredMethods = this.getClass().getDeclaredMethods();
            for (final Method method : declaredMethods) {
                if (Modifier.PUBLIC.ordinal() == method.getModifiers()) {
                    if (method.getAnnotation(ano) != null) {
                        methodsToCheck.add(method);
                    }
                }
            }

            return methodsToCheck;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.METHOD })
        private @interface ValidateNotNull {

            String errorParameterMsg();
        }

        public static class InvalidTestStoreException extends Exception {

            private static final long serialVersionUID = 3123604043036477588L;

            public InvalidTestStoreException(final List<String> errors) {
                super(Joiner.on("\nerror: ").join(errors));
            }
        }
    }
}
