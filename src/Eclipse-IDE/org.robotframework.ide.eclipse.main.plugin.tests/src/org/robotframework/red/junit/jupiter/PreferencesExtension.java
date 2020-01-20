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
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

/**
 * <p>
 * This extension is used to manipulate RED preferences. This extension should
 * be used together with {@link StringPreference}, {@link IntegerPreference} or
 * {@link BooleanPreference} annotations. The
 * annotations can be used on methods in order to set preferences before the test. After test is
 * executed the changed preferences will be reverted to default.
 * </p>
 * <p>
 * Additionally fields of types {@link PreferencesUpdater} annotated with {@link Managed} will be
 * injected
 * in order to manipulate preferences inside the test. After test will end also those preferences
 * will be
 * reverted to default.
 * </p>
 * 
 * @author anglart
 */
public class PreferencesExtension extends ManagedStateExtension
        implements Extension, BeforeEachCallback, AfterEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(PreferencesExtension.class);
    private static final String NAMES = "prefs.names";

    @Override
    protected Class<? extends ManagedState> getManagedStateClass() {
        return PreferencesUpdater.class;
    }

    @Override
    protected ManagedState createManagedState() {
        return new PreferencesUpdater(RedPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final IPreferenceStore preferenceStore = RedPlugin.getDefault().getPreferenceStore();

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
        
        context.getStore(NAMESPACE).put(NAMES, names);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        super.afterEach(context);

        for (final Object name : (Set<?>) context.getStore(NAMESPACE).get(NAMES)) {
            RedPlugin.getDefault().getPreferenceStore().setToDefault((String) name);
        }
    }
}
