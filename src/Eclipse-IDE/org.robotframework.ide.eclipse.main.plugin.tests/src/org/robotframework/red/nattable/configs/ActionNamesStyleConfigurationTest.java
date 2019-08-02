/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE))
                .thenReturn(new ColoringPreference(new RGB(10, 11, 12), SWT.BOLD));
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.KEYWORD_CALL_LIBRARY))
                .thenReturn(new ColoringPreference(new RGB(13, 14, 15), SWT.BOLD));
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
        assertThat(styles.asMapOfRanges()).isEmpty();
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
                .apply("&{var@{${ia}ble}} sth} &{another}[index]]variable {nonvariable} @{list}");
        assertThat(styles.asMapOfRanges()).hasSize(4)
                .hasEntrySatisfying(Range.closedOpen(0, 17), styler -> hasForeground(styler, new RGB(4, 5, 6)))
                .hasEntrySatisfying(Range.closedOpen(23, 32), styler -> hasForeground(styler, new RGB(4, 5, 6)))
                .hasEntrySatisfying(Range.closedOpen(32, 40), styler -> hasForeground(styler, new RGB(4, 5, 6)))
                .hasEntrySatisfying(Range.closedOpen(64, 71), styler -> hasForeground(styler, new RGB(4, 5, 6)));
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
        assertThat(styles.asMapOfRanges()).hasSize(1)
                .hasEntrySatisfying(Range.closedOpen(0, 6), styler -> hasForeground(styler, new RGB(7, 8, 9)));
    }

    @Test
    public void rangeStylesFunctionForGherkinsIsDefinedAndProperlyFindsGherkins_whenThereAreMultipleGherkins() {
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
                .apply("When Then Start Keyword Execution");
        assertThat(styles.asMapOfRanges()).hasSize(1)
                .hasEntrySatisfying(Range.closedOpen(0, 10), styler -> hasForeground(styler, new RGB(7, 8, 9)));
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
        assertThat(styles.asMapOfRanges()).hasSize(4)
                .hasEntrySatisfying(Range.closedOpen(0, 5), styler -> hasForeground(styler, new RGB(7, 8, 9)))
                .hasEntrySatisfying(Range.closedOpen(8, 25), styler -> hasForeground(styler, new RGB(4, 5, 6)))
                .hasEntrySatisfying(Range.closedOpen(31, 40), styler -> hasForeground(styler, new RGB(4, 5, 6)))
                .hasEntrySatisfying(Range.closedOpen(40, 48), styler -> hasForeground(styler, new RGB(4, 5, 6)));
    }

    @Test
    public void rangeStylesFunctionForQuotesIsDefinedAndProperlyFindsQuotes_whenThereAreQuotes() {
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
                .apply("An \"embedded\" keyword with \"x\", \"xy\" and \"xyz\" arguments ending with single \" quote");
        assertThat(styles.asMapOfRanges()).hasSize(4)
                .hasEntrySatisfying(Range.closedOpen(3, 13), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(27, 30), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(32, 36), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(41, 46), styler -> hasForeground(styler, new RGB(10, 11, 12)));
    }

    @Test
    public void rangeStylesFunctionForQuotesAndVariablesIsDefinedAndProperlyFindsThose_whenThereAreSuchElements() {
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
                .apply("An \"embedded\" keyword with \"x\", \"x ${variable} y\" and ${other} arguments ending with \"");
        assertThat(styles.asMapOfRanges()).hasSize(4)
                .hasEntrySatisfying(Range.closedOpen(3, 13), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(27, 30), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(35, 46), styler -> hasForeground(styler, new RGB(4, 5, 6)))
                .hasEntrySatisfying(Range.closedOpen(54, 62), styler -> hasForeground(styler, new RGB(4, 5, 6)));
    }

    @Test
    public void rangeStylesFunctionForGherkinsQuotesAndVariablesIsDefinedAndProperlyFindsThose_whenThereAreSuchElements() {
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
                .apply("Then start kw with \"x\" and ${other} arguments");
        assertThat(styles.asMapOfRanges()).hasSize(3)
                .hasEntrySatisfying(Range.closedOpen(0, 5), styler -> hasForeground(styler, new RGB(7, 8, 9)))
                .hasEntrySatisfying(Range.closedOpen(19, 22), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(27, 35), styler -> hasForeground(styler, new RGB(4, 5, 6)));
    }

    @Test
    public void rangeStylesFunctionForLibraryPrefixIsDefinedAndProperlyFindsLibraryPrefix_whenThereIsLibraryPrefix() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final ActionNamesStyleConfiguration config = new ActionNamesStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction.apply("SomeLibrary.keywordCall");
        assertThat(styles.asMapOfRanges()).hasSize(1)
                .hasEntrySatisfying(Range.closedOpen(0, 12), styler -> hasForeground(styler, new RGB(13, 14, 15)));
    }

    @Test
    public void rangeStylesFunctionForGherkinsLibraryPrefixQuotesAndVariablesIsDefinedAndProperlyFindsThose_whenThereAreSuchElements() {
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
                .apply("When SomeLibrary.With.Dots.KeywordCall With \"arg.1\" And ${v.A.r} Embedded");
        assertThat(styles.asMapOfRanges()).hasSize(6)
                .hasEntrySatisfying(Range.closedOpen(0, 5), styler -> hasForeground(styler, new RGB(7, 8, 9)))
                .hasEntrySatisfying(Range.closedOpen(5, 17), styler -> hasForeground(styler, new RGB(13, 14, 15)))
                .hasEntrySatisfying(Range.closedOpen(17, 22), styler -> hasForeground(styler, new RGB(13, 14, 15)))
                .hasEntrySatisfying(Range.closedOpen(22, 27), styler -> hasForeground(styler, new RGB(13, 14, 15)))
                .hasEntrySatisfying(Range.closedOpen(44, 51), styler -> hasForeground(styler, new RGB(10, 11, 12)))
                .hasEntrySatisfying(Range.closedOpen(56, 64), styler -> hasForeground(styler, new RGB(4, 5, 6)));
    }

    private void hasForeground(final Styler styler, final RGB rgb) {
        final TextStyle styleToCheck = new TextStyle();
        styler.applyStyles(styleToCheck);
        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(rgb);
    }
}
