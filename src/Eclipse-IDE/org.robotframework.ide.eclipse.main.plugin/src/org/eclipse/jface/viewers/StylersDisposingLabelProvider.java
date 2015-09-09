/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;

public abstract class StylersDisposingLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    private final List<DisposeNeededStyler> stylersToDispose = new ArrayList<>();

    @Override
    public void dispose() {
        for (final DisposeNeededStyler styler : stylersToDispose) {
            styler.dispose();
        }
        stylersToDispose.clear();
    };

    protected final DisposeNeededStyler addDisposeNeededStyler(final DisposeNeededStyler styler) {
        stylersToDispose.add(styler);
        return styler;
    }
}