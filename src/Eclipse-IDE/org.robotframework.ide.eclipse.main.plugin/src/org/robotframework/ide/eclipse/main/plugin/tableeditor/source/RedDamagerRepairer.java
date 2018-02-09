/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Iterator;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokenScanner.UnableToScanTokensException;
import org.robotframework.red.swt.SwtThread;

class RedDamagerRepairer extends DefaultDamagerRepairer {

    private final SourceViewer viewer;

    RedDamagerRepairer(final ITokenScanner scanner, final SourceViewer sourceViewer) {
        super(scanner);
        this.viewer = sourceViewer;
    }

    @Override
    public void createPresentation(final TextPresentation presentation, final ITypedRegion region) {
        try {
            super.createPresentation(presentation, region);
            fixOverlappingRanges(presentation);
        } catch (final UnableToScanTokensException e) {
            // lets try to repaint
            SwtThread.asyncExec(() -> viewer.invalidateTextPresentation());
        }
    }

    // sometimes it may happen that ranges are overlapping when
    // one thread cleared tokens stored after reparsing while
    // repainting is done; this will be repainted and fixed
    // anyway in subsequent calls, but we don't want exceptions
    // being thrown in such situations
    private void fixOverlappingRanges(final TextPresentation presentation) {
        final Iterator<?> iterator = presentation.getAllStyleRangeIterator();
        StyleRange prev = null;
        while (iterator.hasNext()) {
            final Object next = iterator.next();
            final StyleRange range = (StyleRange) next;
            if (prev != null && prev.start + prev.length > range.start) {
                range.start = prev.start + prev.length;
            }
            prev = range;
        }
    }
}
