/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class PreferenceUpdater implements TestRule {

    private final Set<String> preferenceNames = new HashSet<>();

    private final IPreferenceStore preferenceStore = RedPlugin.getDefault().getPreferenceStore();

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    preferenceNames.forEach(name -> preferenceStore.setToDefault(name));
                    preferenceNames.clear();
                }
            }
        };
    }

    public void setValue(final String name, final double value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final float value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final int value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final long value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final String value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

    public void setValue(final String name, final boolean value) {
        preferenceStore.setValue(name, value);
        preferenceNames.add(name);
    }

}
