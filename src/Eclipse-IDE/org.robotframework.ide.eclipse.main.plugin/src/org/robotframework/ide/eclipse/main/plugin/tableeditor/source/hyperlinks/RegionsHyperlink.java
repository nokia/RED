/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * @author mmarzec
 */
public class RegionsHyperlink implements IHyperlink {

    private final ITextViewer viewer;

    private final IRegion source;

    private final IRegion destination;

    public RegionsHyperlink(final ITextViewer viewer, final IRegion from, final IRegion to) {
        this.source = from;
        this.destination = to;
        this.viewer = viewer;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return source;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return null;
    }

    @Override
    public void open() {
        try {
            final int topIndexPosition = viewer.getDocument().getLineOfOffset(destination.getOffset());
            viewer.getTextWidget().setTopIndex(topIndexPosition);
            viewer.getTextWidget().setSelection(destination.getOffset(), destination.getOffset() + destination.getLength());

        } catch (final BadLocationException e) {
            throw new IllegalArgumentException("Cannot open hyperlink", e);
        }
    }
}
