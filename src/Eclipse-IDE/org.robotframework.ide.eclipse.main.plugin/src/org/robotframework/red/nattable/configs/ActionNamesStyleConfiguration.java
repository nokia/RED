/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.eclipse.jface.viewers.Stylers.mixingStyler;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
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
public class ActionNamesStyleConfiguration extends RobotElementsStyleConfiguration {

    public ActionNamesStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    ActionNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style actionStyle = createStyle(SyntaxHighlightingCategory.KEYWORD_CALL);
        augmentActionNamesStyleWithGherkinsAndVariables(actionStyle, preferences);

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, actionStyle, mode,
                    ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        });
    }

    public static void augmentActionNamesStyleWithGherkinsAndVariables(final Style style,
            final RedPreferences preferences) {
        final ColoringPreference gherkinColoring = preferences.getSyntaxColoring(SyntaxHighlightingCategory.GHERKIN);
        final ColoringPreference variableColoring = preferences.getSyntaxColoring(SyntaxHighlightingCategory.VARIABLE);
        final Styler gherkinStyler = mixingStyler(Stylers.withForeground(gherkinColoring.getRgb()),
                Stylers.withFontStyle(gherkinColoring.getFontStyle()));
        final Styler variableStyler = mixingStyler(Stylers.withForeground(variableColoring.getRgb()),
                Stylers.withFontStyle(variableColoring.getFontStyle()));

        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findGherkinsAndVariables(gherkinStyler, variableStyler));
    }

    private static Function<String, RangeMap<Integer, Styler>> findGherkinsAndVariables(final Styler gherkinStyler,
            final Styler variableStyler) {
        return label -> {
            final TreeRangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            final Optional<Range<Integer>> gherkinRange = markGherkin(label);
            if (gherkinRange.isPresent()) {
                mapping.put(gherkinRange.get(), gherkinStyler);
            }
            for (final Range<Integer> varRange : VariableInsideStyleConfiguration.markVariables(label)) {
                mapping.put(varRange, variableStyler);
            }
            return mapping;
        };
    }

    private static Optional<Range<Integer>> markGherkin(final String label) {
        int endOfGherkin = 0;
        final String toCompare = label.toLowerCase();
        for (final String prefix : GherkinStyleSupport.PREFIXES) {
            if (toCompare.startsWith(prefix + " ")) {
                endOfGherkin = prefix.length();
            }
        }
        return endOfGherkin == 0 ? Optional.empty() : Optional.of(Range.closedOpen(0, endOfGherkin));
    }
}
