/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.StringAsArgExtractor;

public class CallArgumentsBinderTest {

    @Test
    public void cannotBindWhenDescriptorIsCorrupted() {
        assertThat(CallArgumentsBinder.canBind(new RobotVersion(3, 1), ArgumentsDescriptor.createDescriptor("a", "a")));
        assertThat(
                CallArgumentsBinder.canBind(new RobotVersion(3, 1), ArgumentsDescriptor.createDescriptor("a=1", "b")));
        assertThat(CallArgumentsBinder.canBind(new RobotVersion(3, 1),
                ArgumentsDescriptor.createDescriptor("*varargs1", "*varargs2")));
        assertThat(CallArgumentsBinder.canBind(new RobotVersion(3, 1),
                ArgumentsDescriptor.createDescriptor("**kwargs1", "**kwargs2")));
    }

    @Test
    public void canBindWhenDescriptorIsValid() {
        assertThat(CallArgumentsBinder.canBind(new RobotVersion(3, 1), ArgumentsDescriptor.createDescriptor("a", "b")));
        assertThat(
                CallArgumentsBinder.canBind(new RobotVersion(3, 1), ArgumentsDescriptor.createDescriptor("a", "b=2")));
        assertThat(CallArgumentsBinder.canBind(new RobotVersion(3, 1),
                ArgumentsDescriptor.createDescriptor("*varargs", "**kwargs")));
    }

    @Test
    public void newlyCreatedBinderHasNoBindings() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor();
        final CallArgumentsBinder<String> binder = createBinder(descriptor);

        assertThat(binder.hasBindings()).isFalse();
    }

    @Test
    public void noBindingsAreAvailable_whenOrderOfNamedAndPositionalIsWrong() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b");
        final CallArgumentsBinder<String> binder = createBinder(descriptor);

        binder.bind(newArrayList("a=1", "2"));

        assertThat(binder.hasBindings()).isFalse();
    }

    @Test
    public void noBindingsAreAvailable_whenPositionalArgIsDuplicatedByNamed() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b");
        final CallArgumentsBinder<String> binder = createBinder(descriptor);

        binder.bind(newArrayList("1", "a=2"));

        assertThat(binder.hasBindings()).isFalse();
    }

    @Test
    public void noBindingsAreAvailable_whenNumberOfArgumentsIsWrong() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b");
        final CallArgumentsBinder<String> binder = createBinder(descriptor);

        binder.bind(newArrayList("1"));

        assertThat(binder.hasBindings()).isFalse();
    }

    @Test
    public void thereAreBindingsAvailable_1() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b");
        final CallArgumentsBinder<String> binder = createBinder(descriptor);

        binder.bind(newArrayList("1", "2"));

        assertThat(binder.hasBindings()).isTrue();
        assertThat(binder.getLastBindedTo(descriptor.get(0))).contains("1");
        assertThat(binder.getLastBindedTo(descriptor.get(1))).contains("2");
        assertThat(binder.getLastValueBindedTo(descriptor.get(0))).contains("1");
        assertThat(binder.getLastValueBindedTo(descriptor.get(1))).contains("2");
        assertThat(binder.getValuesBindedTo(descriptor.get(0))).containsExactly("1");
        assertThat(binder.getValuesBindedTo(descriptor.get(1))).containsExactly("2");
    }

    @Test
    public void thereAreBindingsAvailable_2() {
        final ArgumentsDescriptor descriptor = ArgumentsDescriptor.createDescriptor("a", "b");
        final CallArgumentsBinder<String> binder = createBinder(descriptor);

        binder.bind(newArrayList("b=1", "a=2"));

        assertThat(binder.hasBindings()).isTrue();
        assertThat(binder.getLastBindedTo(descriptor.get(0))).contains("a=2");
        assertThat(binder.getLastBindedTo(descriptor.get(1))).contains("b=1");
        assertThat(binder.getLastValueBindedTo(descriptor.get(0))).contains("2");
        assertThat(binder.getLastValueBindedTo(descriptor.get(1))).contains("1");
        assertThat(binder.getValuesBindedTo(descriptor.get(0))).containsExactly("2");
        assertThat(binder.getValuesBindedTo(descriptor.get(1))).containsExactly("1");
    }

    private static CallArgumentsBinder<String> createBinder(final ArgumentsDescriptor descriptor) {
        return new CallArgumentsBinder<>(new StringAsArgExtractor(), descriptor);
    }
}
