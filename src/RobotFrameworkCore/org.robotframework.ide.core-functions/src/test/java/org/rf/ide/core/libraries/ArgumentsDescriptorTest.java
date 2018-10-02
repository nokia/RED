/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;


public class ArgumentsDescriptorTest {

    @Test
    public void requiredArgumentDescriptorTest_1() {
        final ArgumentsDescriptor desc = ArgumentsDescriptor.createDescriptor("a");
 
        assertThat(desc.isValid()).isTrue();
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

        assertThat(desc.isValid()).isTrue();
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

        assertThat(desc.isValid()).isTrue();
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

        assertThat(desc.isValid()).isTrue();
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

        assertThat(desc.isValid()).isTrue();
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

        assertThat(desc.isValid()).isTrue();
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
