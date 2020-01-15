/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * <p>
 * JUnit Jupiter extension used to provide fresh {@link Shell} instances. This extension should
 * be used together with {@link FreshShell} annotation in order to inject shells. The annotation
 * can be used on both static and non-static non-final fields as well as test method parameters. The
 * type of a field/parameter has to be {@link Shell} or its super class. When annotated:
 * </p>
 * <ul>
 * <li>static field will be initialized before any test starts and the shell will remain open until
 * all tests ends; it will be closed and disposed after all tests,
 * </li>
 * <li>non-static field will be initialized before each test start and it will remain open during
 * test execution; it will be closed and disposed after each test ends,
 * </li>
 * <li>parameter will be initialized similarly as in case of non-static fields but on test method
 * level.
 * </li>
 * </ul>
 * 
 * @author anglart
 */
public class FreshShellExtension implements Extension, BeforeAllCallback, BeforeEachCallback, AfterEachCallback,
        AfterAllCallback, ParameterResolver {

    private static final Namespace RED_NAMESPACE = Namespace.create("red");
    private static final String SHELL_PARAM = "shell.param";

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), true, FreshShell.class,
                createAndSetNewShell(null));
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final Object testInstance = context.getRequiredTestInstance();
        FieldsSupport.handleFields(testInstance.getClass(), false, FreshShell.class,
                createAndSetNewShell(testInstance));
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final Object testInstance = context.getRequiredTestInstance();
        FieldsSupport.handleFields(testInstance.getClass(), false, FreshShell.class, disposeOldShell(testInstance));
        
        final Object shell = context.getStore(RED_NAMESPACE).get(SHELL_PARAM);
        if (shell instanceof Shell && !(((Shell) shell).isDisposed())) {
            ((Shell) shell).close();
            ((Shell) shell).dispose();
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        FieldsSupport.handleFields(context.getRequiredTestClass(), true, FreshShell.class, disposeOldShell(null));
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        final boolean annotated = parameterContext.isAnnotated(FreshShell.class);
        if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
            throw new ParameterResolutionException("@" + FreshShell.class.getSimpleName()
                    + " is not supported on constructor parameters. Please use field injection instead.");
        }
        return annotated;
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
            throws ParameterResolutionException {
        final Class<?> parameterType = parameterContext.getParameter().getType();
        assertSupportedType("parameter", parameterType);

        final Shell shell = new Shell(Display.getDefault());
        shell.open();

        extensionContext.getStore(RED_NAMESPACE).put(SHELL_PARAM, shell);
        return shell;
    }

    private static void assertSupportedType(final String target, final Class<?> type) {
        if (!type.isAssignableFrom(Shell.class)) {
            throw new ExtensionConfigurationException(
                    "Can only resolve @" + FreshShell.class.getSimpleName() + " " + target
                    + " of type " + Shell.class.getName() + " or its super classes but was: " + type.getName());
        }
    }

    private static Consumer<Field> createAndSetNewShell(final Object instance) {
        return field -> {
            assertSupportedType("field", field.getType());

            final Shell shell = new Shell(Display.getDefault());
            try {
                field.set(instance, shell);
                shell.open();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                shell.dispose();
            }
        };
    }

    private static Consumer<Field> disposeOldShell(final Object instance) {
        return field -> {
            try {
                final Object shell = field.get(instance);
                if (shell instanceof Shell && !(((Shell) shell).isDisposed())) {
                    ((Shell) shell).close();
                    ((Shell) shell).dispose();
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // can't happen
            }
        };
    }
}
