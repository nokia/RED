/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This extension injects launch configuration into {@link ILaunchConfiguration} or
 * {@link ILaunchConfigurationWorkingCopy} fields (both static and non-static) and takes care of
 * removing them from {@link ILaunchManager} when they are no longer needed.
 * 
 * @author anglart
 */
public class LaunchConfigExtension implements Extension, BeforeAllCallback, BeforeEachCallback, AfterEachCallback,
        AfterAllCallback {

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), true, LaunchConfig.class,
                createAndSetNewLaunchConfig(null));
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), false, LaunchConfig.class,
                createAndSetNewLaunchConfig(context.getRequiredTestInstance()));
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), false, LaunchConfig.class,
                removeLaunchConfig(context.getRequiredTestInstance()));
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), true, LaunchConfig.class, removeLaunchConfig(null));
    }

    private static void assertSupportedType(final String target, final Class<?> type) {
        if (type != ILaunchConfiguration.class && type != ILaunchConfigurationWorkingCopy.class) {
            throw new ExtensionConfigurationException(
                    "Can only resolve @" + LaunchConfig.class.getSimpleName() + " " + target + " of type "
                            + ILaunchConfiguration.class.getName() + " or "
                            + ILaunchConfigurationWorkingCopy.class.getName() + " but was: " + type.getName());
        }
    }

    private static Consumer<Field> createAndSetNewLaunchConfig(final Object instance) {
        return field -> {
            assertSupportedType("field", field.getType());

            final LaunchConfig annotatedCfg = field.getAnnotation(LaunchConfig.class);
            final String cfgTypeId = annotatedCfg.typeId();

            final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            final ILaunchConfigurationType typeId = launchManager.getLaunchConfigurationType(cfgTypeId);

            try {
                final ILaunchConfigurationWorkingCopy config = typeId.newInstance(null, annotatedCfg.name());
                field.set(instance, config);
            } catch (final CoreException | IllegalArgumentException | IllegalAccessException e) {
            }
        };
    }

    private static Consumer<Field> removeLaunchConfig(final Object instance) {
        return field -> {
            try {
                final Object launchCfg = field.get(instance);
                if (launchCfg instanceof ILaunchConfiguration) {
                    ((ILaunchConfiguration) launchCfg).delete();
                }
            } catch (final CoreException | IllegalArgumentException | IllegalAccessException e) {
            }
        };
    }
}
