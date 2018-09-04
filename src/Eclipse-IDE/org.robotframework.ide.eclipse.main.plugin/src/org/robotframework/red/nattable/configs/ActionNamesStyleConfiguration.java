/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.function.Function;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;

/**
 * @author lwlodarc
 */
public class ActionNamesStyleConfiguration extends RobotElementsStyleConfiguration {

    public ActionNamesStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    ActionNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    String getConfigLabel() {
        return ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = createStyle(SyntaxHighlightingCategory.KEYWORD_CALL);
        final Styler gherkinStyler = createStyler(SyntaxHighlightingCategory.GHERKIN);
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findGherkinsAndVariables(gherkinStyler, variableStyler));
        return style;
    }

    private static Function<String, RangeMap<Integer, Styler>> findGherkinsAndVariables(final Styler gherkinStyler,
            final Styler variableStyler) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            final Range<Integer> gherkinRange = markGherkin(label);
            if (!gherkinRange.isEmpty()) {
                mapping.put(gherkinRange, gherkinStyler);
            }
            final RangeSet<Integer> varRanges = VariablesInNamesStyleConfiguration.markVariables(label,
                    VariablesInNamesStyleConfiguration.VAR_IN_NAMES_PATTERN);
            for (final Range<Integer> varRange : varRanges.asRanges()) {
                mapping.put(varRange, variableStyler);
            }
            return mapping;
        };
    }

    private static Range<Integer> markGherkin(final String label) {
        final String textAfterPrefix = GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(label);
        return Range.closedOpen(0, label.length() - textAfterPrefix.length());
    }
}
