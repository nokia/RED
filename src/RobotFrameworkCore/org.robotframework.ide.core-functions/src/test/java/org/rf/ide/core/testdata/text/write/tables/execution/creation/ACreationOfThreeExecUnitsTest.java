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
import org.rf.ide.core.testdata.text.write.tables.execution.creation.ACreationOfThreeExecUnitsTest.TestFilesCompareStore.InvalidTestStoreException;

import com.google.common.base.Joiner;

public abstract class ACreationOfThreeExecUnitsTest {

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

    @SuppressWarnings("unchecked")
    private void assert_three_execUnits_noCommentedLines_template(
            final List<IExecutableStepsHolder<? extends AModelElement<? extends ARobotSectionTable>>> execUnit,
            final TestFilesCompareStore fileStore) throws Exception {
        // prepare
        checkEnvironment();
        RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>> execUnitOneExecRowOne = (RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>>) execUnit
                .get(0).getExecutionContext().get(0);
        RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>> execUnitTwoExecRowOne = (RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>>) execUnit
                .get(1).getExecutionContext().get(0);
        RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>> execUnitThreeExecRowOne = (RobotExecutableRow<? extends AModelElement<? extends ARobotSectionTable>>) execUnit
                .get(2).getExecutionContext().get(0);

        // test data prepare
        //// exec unit 1
        ///// line 1
        RobotToken action1 = new RobotToken();
        action1.setText("execAction1");
        execUnitOneExecRowOne.setAction(action1);

        //// exec unit 2
        ///// line 1
        RobotToken action2 = new RobotToken();
        action2.setText("execAction2");
        execUnitTwoExecRowOne.setAction(action2);

        RobotToken arg1_2 = new RobotToken();
        arg1_2.setText("arg1");
        RobotToken arg2_2 = new RobotToken();
        arg2_2.setText("arg2");
        RobotToken arg3_2 = new RobotToken();
        arg3_2.setText("arg3");

        execUnitTwoExecRowOne.addArgument(arg1_2);
        execUnitTwoExecRowOne.addArgument(arg2_2);
        execUnitTwoExecRowOne.addArgument(arg3_2);

        //// exec unit 3
        ///// line 1
        RobotToken action3 = new RobotToken();
        action3.setText("execAction3");
        execUnitThreeExecRowOne.setAction(action3);

        RobotToken arg1_3 = new RobotToken();
        arg1_3.setText("arg1a");

        execUnitThreeExecRowOne.addArgument(arg1_3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");

        execUnitThreeExecRowOne.addCommentPart(cm1);
        execUnitThreeExecRowOne.addCommentPart(cm2);
        execUnitThreeExecRowOne.addCommentPart(cm3);
    }

    private void checkEnvironment() throws InvalidTestStoreException {
        final TestFilesCompareStore cmpExecWithName = getCompareFilesStoreForExecutableWithName();
        if (!cmpExecWithName.wasValidated.get()) {
            cmpExecWithName.validate();
        }

        final TestFilesCompareStore cmpExecWithoutName = getCompareFilesStoreForExecutableWithTheFirstWithoutName();
        if (!cmpExecWithoutName.wasValidated.get()) {
            cmpExecWithoutName.validate();
        }
    }

    public static class TestFilesCompareStore {

        private String threeExecUnitsWithOneLineEachOtherInsideCmpFile;

        @ValidateNotNull(errorParameterMsg = "three exec units with one line inside")
        public String getThreeExecUnitsWithOneLineEachOtherInsideCmpFile() {
            return threeExecUnitsWithOneLineEachOtherInsideCmpFile;
        }

        public void setThreeExecUnitsWithOneLineEachOtherInsideCmpFile(
                String threeExecUnitsWithOneLineEachOtherInsideCmpFile) {
            this.threeExecUnitsWithOneLineEachOtherInsideCmpFile = threeExecUnitsWithOneLineEachOtherInsideCmpFile;
        }

        private AtomicBoolean wasValidated = new AtomicBoolean(false);

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
