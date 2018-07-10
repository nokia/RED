/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

/**
 * @author lwlodarc
 *
 */
public class VariablesInNamesStyleConfigurationTest {

    private final RedPreferences preferences = mock(RedPreferences.class);

    @Before
    public void before() {
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.VARIABLE))
                .thenReturn(new ColoringPreference(new RGB(1, 2, 3), SWT.NORMAL));
    }

    @Test
    public void sameStyleIsRegisteredForEachDisplayMode() throws Exception {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final VariablesInNamesStyleConfiguration config = new VariablesInNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style1 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
        final IStyle style2 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
        final IStyle style3 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
        final IStyle style4 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                DisplayMode.SELECT_HOVER, VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);

        assertThat(style1).isSameAs(style2);
        assertThat(style2).isSameAs(style3);
        assertThat(style3).isSameAs(style4);
    }

    @Test
    public void rangeStylesFunctionForVariablesIsDefinedButDoesNotFindAnyVariable_whenThereAreNoVariables() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final VariablesInNamesStyleConfiguration config = new VariablesInNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction.apply("the}[re] is${ no var{iable");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).isEmpty();
    }

    @Test
    public void rangeStylesFunctionForVariablesIsDefinedAndProperlyFindsVariables_whenThereAreMultipleSophisticatedVariables() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final VariablesInNamesStyleConfiguration config = new VariablesInNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction
                .apply("&{var@{${ia}ble}} sth} &{another}[index]]variable {nonvariable}");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).hasSize(2);
        assertThat(stylesAsMap.keySet()).containsExactly(Range.closedOpen(0, 17), Range.closedOpen(23, 40));

        final TextStyle styleToCheck = new TextStyle();
        stylesAsMap.values().forEach(styler -> styler.applyStyles(styleToCheck));

        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(1, 2, 3));
    }

    @Test
    public void rangeStylesFunctionForVariablesIsDefinedAndProperlyFindsVariables_whenThereIsSingleVariable() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final VariablesInNamesStyleConfiguration config = new VariablesInNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction
                .apply("some${var}text");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).hasSize(1);
        assertThat(stylesAsMap.keySet()).containsExactly(Range.closedOpen(4, 10));

        final TextStyle styleToCheck = new TextStyle();
        stylesAsMap.values().forEach(styler -> styler.applyStyles(styleToCheck));

        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(1, 2, 3));
    }

    @Test
    public void rangeStylesFunctionForVariablesIsDefinedAndThereIsNoVariable_whenThereIsSingleEnvironmentVariable() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final VariablesInNamesStyleConfiguration config = new VariablesInNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction
                .apply("some%{home}text");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).isEmpty();
    }
}
