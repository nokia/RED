/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Supplier;


class CodeSettingsArgumentsLabelProvider extends MatchesHighlightingLabelProvider {

    private final int index;

    CodeSettingsArgumentsLabelProvider(final Supplier<HeaderFilterMatchesCollection> matchesProvider, final int index) {
        super(matchesProvider);
        this.index = index;
    }

    @Override
    public Color getBackground(final Object element) {
        return getSetting(element) == null ? ColorsManager.getColor(250, 250, 250) : null;
    }

    @Override
    public String getText(final Object element) {
        final RobotDefinitionSetting setting = getSetting(element);
        if (setting == null) {
            return "";
        }
        final List<String> arguments = setting.getArguments();
        return index < arguments.size() ? arguments.get(index) : "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return highlightMatches(new StyledString(getText(element)));
    }

    @Override
    public String getToolTipText(final Object element) {
        final String tooltipText = getText(element);
        return tooltipText.isEmpty() ? "<empty>" : tooltipText;
    }

    @Override
    public Image getToolTipImage(final Object object) {
        return ImagesManager.getImage(RedImages.getTooltipImage());
    }

    private RobotDefinitionSetting getSetting(final Object element) {
        return (RobotDefinitionSetting) ((Entry<?, ?>) element).getValue();
    }
}
