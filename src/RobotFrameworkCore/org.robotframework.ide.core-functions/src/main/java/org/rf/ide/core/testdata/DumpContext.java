/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

public class DumpContext {

    private String preferredSeparator = null;

    private boolean isDirty = true;

    public DumpContext() {
        this(null, true);
    }

    public DumpContext(final String preferredSeparator, final boolean isDirty) {
        this.preferredSeparator = preferredSeparator;
        this.isDirty = isDirty;
    }

    public String getPreferredSeparator() {
        return preferredSeparator;
    }

    public boolean isDirty() {
        return isDirty;
    }
}
