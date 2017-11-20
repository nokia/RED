/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.swt;

import org.eclipse.swt.custom.StyledText;

public class StyledTextWrapper {
    // wrapped for testing purposes

    private final StyledText styledText;

    public StyledTextWrapper(final StyledText styledText) {
        this.styledText = styledText;
    }

    public boolean isDisposed() {
        return styledText == null || styledText.isDisposed();
    }

    public void setRedraw(final boolean setRedraw) {
        styledText.setRedraw(setRedraw);
    }

    public int getHorizontalIndex() {
        return styledText.getHorizontalIndex();
    }

    public void setHorizontalIndex(final int index) {
        styledText.setHorizontalIndex(index);
    }
}
