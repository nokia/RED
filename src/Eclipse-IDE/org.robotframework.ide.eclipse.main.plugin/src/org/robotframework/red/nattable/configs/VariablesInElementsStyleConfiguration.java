/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author lwlodarc
 */
public class VariablesInElementsStyleConfiguration extends RobotElementsStyleConfiguration {

    private static final Pattern VAR_IN_ELEMENTS_PATTERN = Pattern.compile("([$@&%]\\{|\\}(\\[[^\\]]+\\])?)");

    public VariablesInElementsStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    VariablesInElementsStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    String getConfigLabel() {
        return VariablesInElementsLabelAccumulator.POSSIBLE_VARIABLES_IN_ELEMENTS_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = new Style();
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                VariablesInNamesStyleConfiguration.findVariables(VAR_IN_ELEMENTS_PATTERN, variableStyler));
        return style;
    }
}
