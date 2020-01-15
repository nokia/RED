/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

public class PreferencesUpdater implements ManagedState {

    private final IPreferenceStore preferenceStore;

    private final Set<String> preferenceNames = new HashSet<>();

    PreferencesUpdater(final IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;
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

    @Override
    public void cleanUp() {
        for (final String name : preferenceNames) {
            preferenceStore.setToDefault(name);
        }
        preferenceNames.clear();
    }
}
