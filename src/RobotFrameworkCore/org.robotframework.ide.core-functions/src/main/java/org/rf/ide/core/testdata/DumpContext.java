/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

public class DumpContext {

    private String preferedSeparator = null;

    private boolean isDirty = true;

    public DumpContext() {
        this(null, true);
    }

    public DumpContext(final String preferedSeparator, final boolean isDirty) {
        this.preferedSeparator = preferedSeparator;
        this.isDirty = isDirty;
    }

    public String getPreferedSeparator() {
        return preferedSeparator;
    }

    public boolean isDirty() {
        return isDirty;
    }
}
