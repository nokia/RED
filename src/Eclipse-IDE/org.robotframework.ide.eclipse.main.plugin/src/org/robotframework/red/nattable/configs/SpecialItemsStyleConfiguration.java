/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author lwlodarc
 *
 */
public class SpecialItemsStyleConfiguration extends RobotElementsStyleConfiguration {

    public SpecialItemsStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    SpecialItemsStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    String getConfigLabel() {
        return SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        return createStyle(SyntaxHighlightingCategory.SPECIAL);
    }
}