/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.preference.IPreferenceStore;

public class RedPreferences {
    
    private final IPreferenceStore store;

    public RedPreferences(final IPreferenceStore store) {
        this.store = store;
    }

    public static final String OTHER_RUNTIMES = "otherRuntimes";
    public static final String ACTIVE_RUNTIME = "activeRuntime";

    public static final String MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS = "minimalArgsColumns";

    public String getActiveRuntime() {
        return store.getString(ACTIVE_RUNTIME);
    }
    
    public String getAllRuntimes() {
        return store.getString(OTHER_RUNTIMES);
    }
    
    public int getMimalNumberOfArgumentColumns() {
        return store.getInt(MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS);
    }
}
