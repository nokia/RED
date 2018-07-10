/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.eclipse.jface.viewers.Stylers.mixingStyler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * @author lwlodarc
 *
 */
public class VariablesInNamesStyleConfiguration extends RobotElementsStyleConfiguration {

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
        augmentGivenStyleWithVariables(regularStyle, preferences);

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, regularStyle, mode,
                    VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
        });
    }

    public static void augmentGivenStyleWithVariables(final Style style, final RedPreferences preferences) {
        final ColoringPreference variableColoring = preferences.getSyntaxColoring(SyntaxHighlightingCategory.VARIABLE);
        final Styler variableStyler = mixingStyler(Stylers.withForeground(variableColoring.getRgb()),
                Stylers.withFontStyle(variableColoring.getFontStyle()));

        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findVariables(variableStyler));
    }

    private static Function<String, RangeMap<Integer, Styler>> findVariables(final Styler variableStyler) {
        return label -> {
            final TreeRangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            for (final Range<Integer> varRange : markVariables(label)) {
                mapping.put(varRange, variableStyler);
            }
            return mapping;
        };
    }

    public static List<Range<Integer>> markVariables(final String label) {
        final List<Range<Integer>> variableRanges = new ArrayList<Range<Integer>>();
        final Matcher bracketsMatcher = Pattern.compile("([$@&]\\{|\\}(\\[[^\\]]+\\])?)").matcher(label);
        int deepLevel = 0;
        int lastFirstLevelVarStart = -1;
        while (bracketsMatcher.find()) {
            if (bracketsMatcher.group().startsWith("}")) {
                if (--deepLevel == 0) {
                    variableRanges.add(Range.closedOpen(lastFirstLevelVarStart, bracketsMatcher.end()));
                }
                // just in case "}" was used as a nonvariable part
                deepLevel = deepLevel < 0 ? 0 : deepLevel;
            } else {
                if (deepLevel == 0) {
                    lastFirstLevelVarStart = bracketsMatcher.start();
                }
                deepLevel++;
            }
        }
        return variableRanges;
    }
}
