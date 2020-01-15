/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

abstract class ManagedStateExtension
        implements Extension, TestInstancePostProcessor, AfterEachCallback, AfterAllCallback {

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(testInstance.getClass(), true, Managed.class,
                createManagedState(testInstance, this::createManagedState));
        FieldsSupport.handleFields(testInstance.getClass(), false, Managed.class,
                createManagedState(testInstance, this::createManagedState));
    }

    protected abstract Class<? extends ManagedState> getManagedStateClass();

    protected abstract ManagedState createManagedState();

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final Object testInstance = context.getRequiredTestInstance();
        FieldsSupport.handleFields(testInstance.getClass(), false, Managed.class, cleanUp(testInstance));
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), true, Managed.class, cleanUp(null));
    }

    private Consumer<Field> createManagedState(final Object instance, final Supplier<ManagedState> supplier) {
        return field -> {
            if (field.getType() == getManagedStateClass()) {
                try {
                    field.set(instance, supplier.get());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                }
            }
        };
    }

    private Consumer<Field> cleanUp(final Object instance) {
        return field -> {
            try {
                final Object managed = field.get(instance);
                if (getManagedStateClass().isInstance(managed)) {
                    ((ManagedState) managed).cleanUp();
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
            }
        };
    }
}
