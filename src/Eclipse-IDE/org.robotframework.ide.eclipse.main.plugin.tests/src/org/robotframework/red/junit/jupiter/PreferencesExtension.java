/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

/**
 * <p>
 * JUnit Jupiter extension used to manipulate RED preferences. This extension should
 * be used together with {@link StringPreference}, {@link IntegerPreference} or
 * {@link BooleanPreference} annotation or directly when created with {@link RegisterExtension}. The
 * annotations can be used on methods in order to set preferences before the test. After test is
 * executed the changed preferences will be reverted to default. When using directly with
 * {@link RegisterExtension} it is possible to use one of setValue methods to change the preference
 * during test execution. After the test the value will reverted.
 * 
 * @author anglart
 */
public class PreferencesExtension implements Extension, BeforeEachCallback, AfterEachCallback {

    private static final Namespace RED_NAMESPACE = Namespace.create("red");
    private static final String NAMES = "prefs.names";

    private final IPreferenceStore preferenceStore = RedPlugin.getDefault().getPreferenceStore();

    private final Set<String> preferenceNames = new HashSet<>();

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final Method method = context.getRequiredTestMethod();

        final Set<String> names = new HashSet<>();

        final Map<String, String> stringMap = Stream.of(method.getAnnotationsByType(StringPreference.class))
                .collect(toMap(StringPreference::key, StringPreference::value));
        stringMap.forEach(preferenceStore::setValue);
        names.addAll(stringMap.keySet());

        final Map<String, Integer> integerMap = Stream.of(method.getAnnotationsByType(IntegerPreference.class))
                .collect(toMap(IntegerPreference::key, IntegerPreference::value));
        integerMap.forEach(preferenceStore::setValue);
        names.addAll(integerMap.keySet());

        final Map<String, Boolean> booleanMap = Stream.of(method.getAnnotationsByType(BooleanPreference.class))
                .collect(toMap(BooleanPreference::key, BooleanPreference::value));
        booleanMap.forEach(preferenceStore::setValue);
        names.addAll(booleanMap.keySet());
        
        context.getStore(RED_NAMESPACE).put(NAMES, names);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        for (final Object name : (Set<?>) context.getStore(RED_NAMESPACE).get(NAMES)) {
            preferenceNames.add((String) name);
        }
        for (final String name : preferenceNames) {
            preferenceStore.setToDefault(name);
        }
        preferenceNames.clear();
    }

    public void setValue(final String name, final String value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final int value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final boolean value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }
}
