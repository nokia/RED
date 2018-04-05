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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
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
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

/**
 * @author lwlodarc
 *
 */
public class ActionNamesStyleConfigurationTest {

    private final RedPreferences preferences = mock(RedPreferences.class);

    @Before
    public void before() {
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.KEYWORD_CALL))
                .thenReturn(new ColoringPreference(new RGB(1, 2, 3), SWT.BOLD));
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.VARIABLE))
                .thenReturn(new ColoringPreference(new RGB(4, 5, 6), SWT.NORMAL));
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.GHERKIN))
                .thenReturn(new ColoringPreference(new RGB(7, 8, 9), SWT.BOLD));
    }

    @Test
    public void sameStyleIsRegisteredForEachDisplayMode() throws Exception {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style1 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        final IStyle style2 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        final IStyle style3 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        final IStyle style4 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                DisplayMode.SELECT_HOVER, ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        assertThat(style1).isSameAs(style2);
        assertThat(style2).isSameAs(style3);
        assertThat(style3).isSameAs(style4);
    }

    @Test
    public void fontDefinedInStyleUsesFontTakenFromThemeWithStyleDefinedInPreferences() {
        final TableTheme theme = mock(TableTheme.class);
        when(theme.getFont()).thenReturn(JFaceResources.getTextFont());

        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(theme, preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        assertThat(style.getAttributeValue(CellStyleAttributes.FONT))
                .isSameAs(FontsManager.transformFontWithStyle(JFaceResources.getTextFont(), SWT.BOLD));
    }

    @Test
    public void foregroundColorDefinedInStyleUsesColorTakenFromPreferences() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        assertThat(style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR).getRGB()).isEqualTo(new RGB(1, 2, 3));
    }

    @Test
    public void rangeStylesFunctionForVariablesAndGherkinsIsDefinedButDoesNotFindAnything_whenThereAreNoVariablesOrGherkins() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction.apply("the}[re] is${ no var{iable");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).isEmpty();
    }

    @Test
    public void rangeStylesFunctionForVariablesIsDefinedAndProperlyFindsVariables_whenThereAreVariables() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

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

        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(4, 5, 6));
    }

    @Test
    public void rangeStylesFunctionForGherkinsIsDefinedAndProperlyFindsGherkins_whenThereIsGherkin() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction
                .apply("GiVeN keywordCall And sth But foo Then bar When");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).hasSize(1);
        assertThat(stylesAsMap.keySet()).containsExactly(Range.closedOpen(0, 5));

        final TextStyle styleToCheck = new TextStyle();
        stylesAsMap.values().forEach(styler -> styler.applyStyles(styleToCheck));

        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(7, 8, 9));
    }

    @Test
    public void rangeStylesFunctionForGherkinsAndVariablesIsDefinedAndProperlyFindsThose_whenThereAreSuchElements() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction
                .apply("When key&{var@{${ia}ble}} sth} &{another}[index]]variable But {nonvariable} And ${");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();

        assertThat(stylesAsMap).hasSize(3);
        assertThat(stylesAsMap.keySet()).containsExactly(Range.closedOpen(0, 4), Range.closedOpen(8, 25),
                Range.closedOpen(31, 48));

        final TextStyle styleToCheck = new TextStyle();
        stylesAsMap.get(Range.closedOpen(0, 4)).applyStyles(styleToCheck);
        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(7, 8, 9));

        stylesAsMap.get(Range.closedOpen(8, 25)).applyStyles(styleToCheck);
        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(4, 5, 6));

        stylesAsMap.get(Range.closedOpen(31, 48)).applyStyles(styleToCheck);
        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(4, 5, 6));
    }
}
