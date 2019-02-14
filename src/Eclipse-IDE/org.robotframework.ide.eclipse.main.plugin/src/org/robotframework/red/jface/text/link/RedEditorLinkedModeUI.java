/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.text.link;

import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.SWT;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LinkedModeStrategy;

import com.google.common.collect.Iterables;

/**
 * @author Michal Anglart
 */
public class RedEditorLinkedModeUI extends LinkedModeUI {

    public static void enableLinkedMode(final ITextViewer viewer, final Collection<IRegion> regionsToLinkedEdit,
            final LinkedModeStrategy strategy) {
        final LinkedModeUI linkedModeUi = createLinkedMode(viewer, regionsToLinkedEdit);
        if (strategy == LinkedModeStrategy.EXIT_ON_LAST) {
            try {
                final IRegion lastRegion = Iterables.getLast(regionsToLinkedEdit);
                final int lastOffset = lastRegion.getOffset() + lastRegion.getLength();
                linkedModeUi.setExitPosition(viewer, lastOffset, 0, Integer.MAX_VALUE);
                linkedModeUi.setCyclingMode(CYCLE_NEVER);
                linkedModeUi.setExitPolicy((model, event, offset, length) -> {
                    if (event.character == SWT.ESC) {
                        return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                    }
                    return null;
                });
            } catch (final BadLocationException e) {
                // shouldn't happen
            }
        }
        linkedModeUi.enter();
    }

    private static LinkedModeUI createLinkedMode(final ITextViewer viewer,
            final Collection<IRegion> regionsToLinkedEdit) {
        try {
            final LinkedModeModel model = new LinkedModeModel();
            for (final IRegion region : regionsToLinkedEdit) {
                final LinkedPositionGroup group = new LinkedPositionGroup();
                group.addPosition(new LinkedPosition(viewer.getDocument(), region.getOffset(), region.getLength()));
                model.addGroup(group);
            }
            model.forceInstall();
            return new RedEditorLinkedModeUI(model, viewer);
        } catch (final BadLocationException e) {
            throw new IllegalStateException("Invalid location for linked mode regions", e);
        }
    }

    private RedEditorLinkedModeUI(final LinkedModeModel model, final ITextViewer... viewers) {
        super(model, viewers);
    }
}
