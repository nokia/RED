/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

import com.google.common.annotations.VisibleForTesting;

public class ActionFromLibNamesStyleConfiguration extends ActionNamesStyleConfiguration {

    public ActionFromLibNamesStyleConfiguration(final TableTheme theme) {
        this(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    ActionFromLibNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences, SyntaxHighlightingCategory.KEYWORD_CALL_FROM_LIB);
    }

    @Override
    String getConfigLabel() {
        return ActionNamesLabelAccumulator.ACTION_FROM_LIB_NAME_CONFIG_LABEL;
    }
}
