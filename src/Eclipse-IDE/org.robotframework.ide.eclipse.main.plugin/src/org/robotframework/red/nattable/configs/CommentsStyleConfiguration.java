/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * @author lwlodarc
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
    String getConfigLabel() {
        return CommentsLabelAccumulator.COMMENT_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = createStyle(SyntaxHighlightingCategory.COMMENT);
        final Set<String> tags = !preferences.isTasksDetectionEnabled() ? new HashSet<>()
                : preferences.getTaskTagsWithPriorities().keySet();
        if (!tags.isEmpty()) {
            final Styler tasksStyler = createStyler(SyntaxHighlightingCategory.TASKS);
            style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                    findTaskTags(Pattern.compile(String.join("|", tags)), tasksStyler));
        }
        return style;
    }

    private static Function<String, RangeMap<Integer, Styler>> findTaskTags(final Pattern pattern,
            final Styler tasksStyler) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            final Matcher matcher = pattern.matcher(label);
            while (matcher.find()) {
                mapping.put(Range.closedOpen(matcher.start(), matcher.end()), tasksStyler);
            }
            return mapping;
        };
    }
}
