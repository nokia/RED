/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;

import com.google.common.base.Supplier;

class KeywordCallNameLabelProvider extends MatchesHighlightingLabelProvider {

    public KeywordCallNameLabelProvider(final Supplier<HeaderFilterMatchesCollection> matchesProvider) {
        super(matchesProvider);
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            return highlightMatches(new StyledString(keywordCall.getName()));
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            final String name = keywordCall.getName();
            return name.isEmpty() ? "<empty>" : name;
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotKeywordCall) {
            return ImagesManager.getImage(RedImages.getTooltipImage());
        }
        return null;
    }
}
