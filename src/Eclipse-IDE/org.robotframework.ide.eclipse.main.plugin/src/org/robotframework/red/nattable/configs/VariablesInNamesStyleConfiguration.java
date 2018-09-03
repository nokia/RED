/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
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
import com.google.common.collect.TreeRangeSet;

/**
 * @author lwlodarc
 */
public class VariablesInNamesStyleConfiguration extends RobotElementsStyleConfiguration {

    static final Pattern VAR_IN_NAMES_PATTERN = Pattern.compile("([$@&]\\{|\\}(\\[[^\\]]+\\])?)");

    public VariablesInNamesStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    VariablesInNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        // for otherwise not styled elements - just color variables
        final Style regularStyle = new Style();
        augmentGivenStyleWithVariables(regularStyle);

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, regularStyle, mode,
                    VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
        });
    }

    private void augmentGivenStyleWithVariables(final Style style) {
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findVariables(VAR_IN_NAMES_PATTERN, variableStyler));
    }

    static Function<String, RangeMap<Integer, Styler>> findVariables(final Pattern pattern,
            final Styler variableStyler) {
        return label -> {
            final TreeRangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            for (final Range<Integer> varRange : markVariables(label, pattern).asRanges()) {
                mapping.put(varRange, variableStyler);
            }
            return mapping;
        };
    }

    static RangeSet<Integer> markVariables(final String label, final Pattern pattern) {
        final RangeSet<Integer> variableRanges = TreeRangeSet.create();
        final Matcher matcher = pattern.matcher(label);
        int deepLevel = 0;
        int lastFirstLevelVarStart = -1;
        while (matcher.find()) {
            if (matcher.group().startsWith("}")) {
                if (--deepLevel == 0) {
                    variableRanges.add(Range.closedOpen(lastFirstLevelVarStart, matcher.end()));
                }
                // just in case "}" was used as a nonvariable part
                deepLevel = deepLevel < 0 ? 0 : deepLevel;
            } else {
                if (deepLevel == 0) {
                    lastFirstLevelVarStart = matcher.start();
                }
                deepLevel++;
            }
        }
        return variableRanges;
    }
}
