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
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask.Priority;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;


public class CommentsStyleConfigurationTest {

    private final RedPreferences preferences = mock(RedPreferences.class);

    @Before
    public void before() {
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.COMMENT))
                .thenReturn(new ColoringPreference(new RGB(1, 2, 3), SWT.BOLD));
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.TASKS))
                .thenReturn(new ColoringPreference(new RGB(4, 5, 6), SWT.ITALIC));
    }

    @Test
    public void sameStyleIsRegisteredForEachDisplayMode() throws Exception {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final CommentsStyleConfiguration config = new CommentsStyleConfiguration(mock(TableTheme.class), preferences);
        config.configureRegistry(configRegistry);

        final IStyle style1 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
        final IStyle style2 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
        final IStyle style3 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
        final IStyle style4 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                DisplayMode.SELECT_HOVER, CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);

        assertThat(style1).isSameAs(style2);
        assertThat(style2).isSameAs(style3);
        assertThat(style3).isSameAs(style4);
    }

    @Test
    public void fontDefinedInStyleUsesFontTakenFromThemeWithStyleDefinedInPreferences() {
        final TableTheme theme = mock(TableTheme.class);
        when(theme.getFont()).thenReturn(JFaceResources.getTextFont());

        final IConfigRegistry configRegistry = new ConfigRegistry();

        final CommentsStyleConfiguration config = new CommentsStyleConfiguration(theme, preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);

        assertThat(style.getAttributeValue(CellStyleAttributes.FONT))
                .isSameAs(FontsManager.transformFontWithStyle(JFaceResources.getTextFont(), SWT.BOLD));
    }

    @Test
    public void foregroundColorDefinedInStyleUsesColorTakenFromPreferences() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final CommentsStyleConfiguration config = new CommentsStyleConfiguration(mock(TableTheme.class), preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
        
        assertThat(style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR).getRGB()).isEqualTo(new RGB(1, 2, 3));
    }

    @Test
    public void rangeStylesFunctionForTasksIsUndefined_whenTaskDetectionIsDisabled() {
        when(preferences.isTasksDetectionEnabled()).thenReturn(false);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(ImmutableMap.of("TODO", Priority.HIGH));

        final IConfigRegistry configRegistry = new ConfigRegistry();

        final CommentsStyleConfiguration config = new CommentsStyleConfiguration(mock(TableTheme.class), preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNull();
    }

    @Test
    public void rangeStylesFunctionForTasksIsUndefined_whenTaskDetectionIsEnabledButThereAreNoTags() {
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities()).thenReturn(ImmutableMap.of());

        final IConfigRegistry configRegistry = new ConfigRegistry();

        final CommentsStyleConfiguration config = new CommentsStyleConfiguration(mock(TableTheme.class), preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNull();
    }

    @Test
    public void rangeStylesFunctionForTasksIsDefinedAndProperlyFindsTags_whenTaskDetectionIsEnabledAndThereAreTags() {
        when(preferences.isTasksDetectionEnabled()).thenReturn(true);
        when(preferences.getTaskTagsWithPriorities())
                .thenReturn(ImmutableMap.of("TODO", Priority.HIGH, "FIXME", Priority.LOW));

        final IConfigRegistry configRegistry = new ConfigRegistry();

        final CommentsStyleConfiguration config = new CommentsStyleConfiguration(mock(TableTheme.class), preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);

        final Function<String, RangeMap<Integer, Styler>> decoratingFunction = style
                .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);
        assertThat(decoratingFunction).isNotNull();

        final RangeMap<Integer, Styler> styles = decoratingFunction.apply("abc TODO def FIXME ghi");
        final Map<Range<Integer>, Styler> stylesAsMap = styles.asMapOfRanges();
        
        assertThat(stylesAsMap).hasSize(2);
        assertThat(stylesAsMap.keySet()).containsExactly(Range.closedOpen(4, 8), Range.closedOpen(13, 18));
        
        final TextStyle styleToCheck = new TextStyle();
        stylesAsMap.values().forEach(styler -> styler.applyStyles(styleToCheck));
        
        assertThat(styleToCheck.foreground.getRGB()).isEqualTo(new RGB(4, 5, 6));
    }
}
