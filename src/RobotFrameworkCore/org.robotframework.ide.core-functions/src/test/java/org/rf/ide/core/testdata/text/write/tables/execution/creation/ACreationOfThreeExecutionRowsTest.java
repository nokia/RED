/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.execution.creation;

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
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecutionRowsTest.TestFilesCompareStore.InvalidTestStoreException;

import com.google.common.base.Joiner;

public abstract class ACreationOfThreeExecutionRowsTest {

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
        List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execOne = executionContext.get(0);
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execTwo = executionContext.get(1);
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execThree = executionContext.get(2);

        // test data prepare
        //// line 1
        RobotToken action1 = new RobotToken();
        action1.setText("execAction1");
        execOne.setAction(action1);

        //// line 2
        RobotToken action2 = new RobotToken();
        action2.setText("execAction2");
        execTwo.setAction(action2);

        RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execTwo.addArgument(arg1_2);
        execTwo.addArgument(arg2_2);
        execTwo.addArgument(arg3_2);

        //// line 3
        RobotToken action3 = new RobotToken();
        action3.setText("execAction3");
        execThree.setAction(action3);

        RobotToken arg1_3 = new RobotToken();
        arg1_3.setText("arg1a");

        execThree.addArgument(arg1_3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
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
        List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execOne = executionContext.get(0);
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execTwo = executionContext.get(1);

        // test data prepare
        //// line 1

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execOne.addCommentPart(cm1);
        execOne.addCommentPart(cm2);
        execOne.addCommentPart(cm3);

        //// line 2
        RobotToken action2 = new RobotToken();
        action2.setText("execAction1");
        execTwo.setAction(action2);

        RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execTwo.addArgument(arg1_2);
        execTwo.addArgument(arg2_2);
        execTwo.addArgument(arg3_2);

        RobotToken cm1_2 = new RobotToken();
        cm1_2.setText("cm1a");
        RobotToken cm2_2 = new RobotToken();
        cm2_2.setText("cm2a");
        RobotToken cm3_2 = new RobotToken();
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
        List<RobotExecutableRow<AModelElement<? extends ARobotSectionTable>>> executionContext = execUnit
                .getExecutionContext();
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execOne = executionContext.get(0);
        RobotExecutableRow<AModelElement<? extends ARobotSectionTable>> execThree = executionContext.get(2);

        // test data prepare
        //// line 1

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execOne.addCommentPart(cm1);
        execOne.addCommentPart(cm2);
        execOne.addCommentPart(cm3);

        //// line 2
        // just empty

        //// line 3
        RobotToken action2 = new RobotToken();
        action2.setText("execAction1");
        execThree.setAction(action2);

        RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execThree.addArgument(arg1_2);
        execThree.addArgument(arg2_2);
        execThree.addArgument(arg3_2);

        RobotToken cm1_2 = new RobotToken();
        cm1_2.setText("cm1a");
        RobotToken cm2_2 = new RobotToken();
        cm2_2.setText("cm2a");
        RobotToken cm3_2 = new RobotToken();
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
        if (!cmpExecWithName.wasValidated.get()) {
            cmpExecWithName.validate();
        }

        final TestFilesCompareStore cmpExecWithoutName = getCompareFilesStoreForExecutableWithoutName();
        if (!cmpExecWithoutName.wasValidated.get()) {
            cmpExecWithoutName.validate();
        }
    }

    public static class TestFilesCompareStore {

        private String threeLinesWithoutCommentedLineCmpFile;

        private String threeLinesWithCommentAndEmptyLineCmpFile;

        private String threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile;

        @ValidateNotNull(errorParameterMsg = "three lines with comment as first and empty line in the middle")
        public String getThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile() {
            return threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile;
        }

        public void setThreeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile(
                String threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile) {
            this.threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile = threeLinesWithCommentTheFirstEmptyLineInTheMiddleCmpFile;
        }

        @ValidateNotNull(errorParameterMsg = "three lines with commented and empty line")
        public String getThreeLinesWithCommentAndEmptyLineCmpFile() {
            return threeLinesWithCommentAndEmptyLineCmpFile;
        }

        public void setThreeLinesWithCommentAndEmptyLineCmpFile(String threeLinesWithCommentAndEmptyLineCmpFile) {
            this.threeLinesWithCommentAndEmptyLineCmpFile = threeLinesWithCommentAndEmptyLineCmpFile;
        }

        private AtomicBoolean wasValidated = new AtomicBoolean(false);

        @ValidateNotNull(errorParameterMsg = "three lines without commented any of them")
        public String getThreeLinesWithoutCommentedLineCmpFile() {
            return threeLinesWithoutCommentedLineCmpFile;
        }

        public void setThreeLinesWithoutCommentedLineCmpFile(String threeLinesWithoutCommentedLineCmpFile) {
            this.threeLinesWithoutCommentedLineCmpFile = threeLinesWithoutCommentedLineCmpFile;
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
