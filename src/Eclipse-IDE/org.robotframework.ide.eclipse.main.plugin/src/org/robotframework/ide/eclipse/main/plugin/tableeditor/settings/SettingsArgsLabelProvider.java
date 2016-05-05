/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;

import com.google.common.base.Supplier;

class SettingsArgsLabelProvider extends MatchesHighlightingLabelProvider {

    private final int index;
    private final boolean shouldProvideLabelForAddingToken;

    SettingsArgsLabelProvider(final Supplier<HeaderFilterMatchesCollection> matchesProvider, final int index) {
        this(matchesProvider, index, false);
    }

    SettingsArgsLabelProvider(final Supplier<HeaderFilterMatchesCollection> matchesProvider, final int index,
            final boolean shouldProvideLabelForAddingToken) {
        super(matchesProvider);
        this.index = index;
        this.shouldProvideLabelForAddingToken = shouldProvideLabelForAddingToken;
    }

    @Override
    public Image getImage(final Object element) {
        if (shouldProvideLabelForAddingToken && element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return super.getImage(element);
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> arguments = setting.getArguments();
            if (index < arguments.size()) {
                return arguments.get(index);
            }
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotSetting) {
            return highlightMatches(new StyledString(getText(element)));
        } else if (shouldProvideLabelForAddingToken && element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotSetting) {
            final String tooltipText = getText(element);
            return tooltipText.isEmpty() ? "<empty>" : tooltipText;
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotSetting) {
            return ImagesManager.getImage(RedImages.getTooltipImage());
        }
        return null;
    }
}
