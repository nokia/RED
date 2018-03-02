/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.eclipse.jface.viewers.Stylers.mixingStyler;

import java.util.HashSet;
import java.util.Set;
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
public class CommentsStyleConfiguration extends RobotElementsStyleConfiguration {

    public CommentsStyleConfiguration(final TableTheme theme) {
        this(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    CommentsStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style commentStyle = augmentCommentStyleWithTasks(createStyle(SyntaxHighlightingCategory.COMMENT));

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, commentStyle, mode,
                    CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
        });
    }

    private Style augmentCommentStyleWithTasks(final Style commentStyle) {
        final Set<String> tags = !preferences.isTasksDetectionEnabled() ? new HashSet<>()
                : preferences.getTaskTagsWithPriorities().keySet();
        final ColoringPreference tasksColoring = preferences.getSyntaxColoring(SyntaxHighlightingCategory.TASKS);
        if (!tags.isEmpty()) {
            commentStyle.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                    findTaskTags(Pattern.compile(String.join("|", tags)), tasksColoring));
        }
        return commentStyle;
    }

    private static Function<String, RangeMap<Integer, Styler>> findTaskTags(final Pattern pattern,
            final ColoringPreference tasksColoring) {
        return label -> {
            final TreeRangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            final Matcher matcher = pattern.matcher(label);
            while (matcher.find()) {
                final Styler styler = mixingStyler(Stylers.withForeground(tasksColoring.getRgb()),
                        Stylers.withFontStyle(tasksColoring.getFontStyle()));
                mapping.put(Range.closedOpen(matcher.start(), matcher.end()), styler);
            }
            return mapping;
        };
    }
}
