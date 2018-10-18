/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;

import org.junit.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.libraries.ArgumentsDescriptor.ArgumentType;
import org.rf.ide.core.libraries.ArgumentsDescriptor.InvalidArgumentsDescriptorException;
import org.rf.ide.core.testdata.model.RobotVersion;


public class ArgumentsDescriptorTest {

    @Test
    public void descriptorsAreParsedAsExpected() {
        assertThat(ArgumentsDescriptor.createDescriptor()).isEqualTo(new ArgumentsDescriptor(new ArrayList<>(), -1));

        assertThat(ArgumentsDescriptor.createDescriptor("x")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.REQUIRED, "x", null, null)), -1));
        assertThat(ArgumentsDescriptor.createDescriptor("x: int")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.REQUIRED, "x", "int", null)), -1));

        assertThat(ArgumentsDescriptor.createDescriptor("x=1")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.DEFAULT, "x", null, "1")), -1));
        assertThat(ArgumentsDescriptor.createDescriptor("x: int=1")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.DEFAULT, "x", "int", "1")), -1));
        assertThat(ArgumentsDescriptor.createDescriptor("x: annotation=1=2:3=4")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.DEFAULT, "x", "annotation", "1=2:3=4")), -1));

        assertThat(ArgumentsDescriptor.createDescriptor("*v")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.VARARG, "v", null, null)), -1));

        assertThat(ArgumentsDescriptor.createDescriptor("**kw")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.KWARG, "kw", null, null)), -1));

        assertThat(ArgumentsDescriptor.createDescriptor("x", "y=2", "*v", "z=3", "w", "**kw")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(
                        new Argument(ArgumentType.REQUIRED, "x", null, null),
                        new Argument(ArgumentType.DEFAULT, "y", null, "2"),
                        new Argument(ArgumentType.VARARG, "v", null, null),
                        new Argument(ArgumentType.KEYWORD_ONLY, "z", null, "3"),
                        new Argument(ArgumentType.KEYWORD_ONLY, "w", null, null),
                        new Argument(ArgumentType.KWARG, "kw", null, null)), -1));
        assertThat(ArgumentsDescriptor.createDescriptor("x", "y=2", "*", "z=3", "w", "**kw")).isEqualTo(
                new ArgumentsDescriptor(newArrayList(new Argument(ArgumentType.REQUIRED, "x", null, null),
                        new Argument(ArgumentType.DEFAULT, "y", null, "2"),
                        new Argument(ArgumentType.KEYWORD_ONLY, "z", null, "3"),
                        new Argument(ArgumentType.KEYWORD_ONLY, "w", null, null),
                        new Argument(ArgumentType.KWARG, "kw", null, null)), 2));
    }

    @Test
    public void invalidDescriptorsTest() {
        final RobotVersion versionOld = new RobotVersion(3, 0);

        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("a=1", "b").validate(versionOld))
                .withMessage("Order of arguments is wrong");
        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("**a", "b").validate(versionOld))
                .withMessage("Order of arguments is wrong");
        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("**a", "b=2").validate(versionOld))
                .withMessage("Order of arguments is wrong");
        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("**a", "*b").validate(versionOld))
                .withMessage("Order of arguments is wrong");

        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("*a", "*b").validate(versionOld))
                .withMessage("There should be only one vararg");

        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("**a", "**b").validate(versionOld))
                .withMessage("There should be only one kwarg");

        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> ArgumentsDescriptor.createDescriptor("a", "a").validate(versionOld))
                .withMessage("Argument names can't be duplicated");
    }

    @Test
    public void invalidAndValidDescriptors_differencesBetweenVersions() {
        final RobotVersion versionOld = new RobotVersion(3, 0);
        final RobotVersion versionNew = new RobotVersion(3, 1);

        final ArgumentsDescriptor desc1 = ArgumentsDescriptor.createDescriptor("*a", "b");
        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> desc1.validate(versionOld))
                .withMessage("Keyword-only arguments are only supported with Robot Framework 3.1 or newer");
        assertThatCode(() -> desc1.validate(versionNew)).doesNotThrowAnyException();

        final ArgumentsDescriptor desc2 = ArgumentsDescriptor.createDescriptor("*a", "b=2");
        assertThatExceptionOfType(InvalidArgumentsDescriptorException.class)
                .isThrownBy(() -> desc2.validate(versionOld))
                .withMessage("Keyword-only arguments are only supported with Robot Framework 3.1 or newer");
        assertThatCode(() -> desc2.validate(versionNew)).doesNotThrowAnyException();
    }


    @Test
    public void requiredArgumentDescriptorTest_1() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("a");
 
        assertThat(desc.getRequiredArguments()).hasSize(1);
        assertThat(desc.getDefaultArguments()).isEmpty();
        assertThat(desc.getVarargArgument()).isEmpty();
        assertThat(desc.getKwargArgument()).isEmpty();

        final Argument arg = desc.get(0);
        assertThat(arg.getName()).isEqualTo("a");
        assertThat(arg.isRequired()).isTrue();
        assertThat(arg.isDefault()).isFalse();
        assertThat(arg.isVarArg()).isFalse();
        assertThat(arg.isKwArg()).isFalse();
        assertThat(arg.getAnnotation()).isEmpty();
        assertThat(arg.getDefaultValue()).isEmpty();

        assertThat(arg.getDescription()).isEqualTo("a");
    }

    @Test
    public void requiredArgumentDescriptorTest_2() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("a: int");

        assertThat(desc.getRequiredArguments()).hasSize(1);
        assertThat(desc.getDefaultArguments()).isEmpty();
        assertThat(desc.getVarargArgument()).isEmpty();
        assertThat(desc.getKwargArgument()).isEmpty();

        final Argument arg = desc.get(0);
        assertThat(arg.getName()).isEqualTo("a");
        assertThat(arg.isRequired()).isTrue();
        assertThat(arg.isDefault()).isFalse();
        assertThat(arg.isVarArg()).isFalse();
        assertThat(arg.isKwArg()).isFalse();
        assertThat(arg.getAnnotation()).contains("int");
        assertThat(arg.getDefaultValue()).isEmpty();

        assertThat(arg.getDescription()).isEqualTo("a: int");
    }

    @Test
    public void defaultArgumentDescriptorTest_1() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("a=5");

        assertThat(desc.getRequiredArguments()).isEmpty();
        assertThat(desc.getDefaultArguments()).hasSize(1);
        assertThat(desc.getVarargArgument()).isEmpty();
        assertThat(desc.getKwargArgument()).isEmpty();

        final Argument arg = desc.get(0);
        assertThat(arg.getName()).isEqualTo("a");
        assertThat(arg.isRequired()).isFalse();
        assertThat(arg.isDefault()).isTrue();
        assertThat(arg.isVarArg()).isFalse();
        assertThat(arg.isKwArg()).isFalse();
        assertThat(arg.getAnnotation()).isEmpty();
        assertThat(arg.getDefaultValue()).contains("5");

        assertThat(arg.getDescription()).isEqualTo("a=5");
    }

    @Test
    public void defaultArgumentDescriptorTest_2() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("a: int=5");

        assertThat(desc.getRequiredArguments()).isEmpty();
        assertThat(desc.getDefaultArguments()).hasSize(1);
        assertThat(desc.getVarargArgument()).isEmpty();
        assertThat(desc.getKwargArgument()).isEmpty();

        final Argument arg = desc.get(0);
        assertThat(arg.getName()).isEqualTo("a");
        assertThat(arg.isRequired()).isFalse();
        assertThat(arg.isDefault()).isTrue();
        assertThat(arg.isVarArg()).isFalse();
        assertThat(arg.isKwArg()).isFalse();
        assertThat(arg.getAnnotation()).contains("int");
        assertThat(arg.getDefaultValue()).contains("5");

        assertThat(arg.getDescription()).isEqualTo("a: int=5");
    }

    @Test
    public void varargsArgumentDescriptorTest() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("*v");

        assertThat(desc.getRequiredArguments()).isEmpty();
        assertThat(desc.getDefaultArguments()).isEmpty();
        assertThat(desc.getVarargArgument()).isPresent();
        assertThat(desc.getKwargArgument()).isEmpty();

        final Argument arg = desc.get(0);
        assertThat(arg.getName()).isEqualTo("v");
        assertThat(arg.isRequired()).isFalse();
        assertThat(arg.isDefault()).isFalse();
        assertThat(arg.isVarArg()).isTrue();
        assertThat(arg.isKwArg()).isFalse();
        assertThat(arg.getAnnotation()).isEmpty();
        assertThat(arg.getDefaultValue()).isEmpty();

        assertThat(arg.getDescription()).isEqualTo("*v");
    }

    @Test
    public void kwargsArgumentDescriptorTest() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("**kw");

        assertThat(desc.getRequiredArguments()).isEmpty();
        assertThat(desc.getDefaultArguments()).isEmpty();
        assertThat(desc.getVarargArgument()).isEmpty();
        assertThat(desc.getKwargArgument()).isPresent();

        final Argument arg = desc.get(0);
        assertThat(arg.getName()).isEqualTo("kw");
        assertThat(arg.isRequired()).isFalse();
        assertThat(arg.isDefault()).isFalse();
        assertThat(arg.isVarArg()).isFalse();
        assertThat(arg.isKwArg()).isTrue();
        assertThat(arg.getAnnotation()).isEmpty();
        assertThat(arg.getDefaultValue()).isEmpty();

        assertThat(arg.getDescription()).isEqualTo("**kw");
    }

}
