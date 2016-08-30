/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata;

public class DumpContext {

    private String preferedSeparator = null;

    private boolean isDirty = true;

    public void setPreferedSeparator(final String preferedSeparator) {
        this.preferedSeparator = preferedSeparator;
    }

    public String getPreferedSeparator() {
        return this.preferedSeparator;
    }

    public void setDirtyFlag(final boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean isDirty() {
        return this.isDirty;
    }
}
