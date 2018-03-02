package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static java.util.stream.Collectors.toMap;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule.ITodoTaskToken;
import org.robotframework.red.graphics.ColorsManager;

import com.google.common.collect.ImmutableSet;

public class ColoringTokens {

    private final Map<SyntaxHighlightingCategory, Token> tokens;

    private final RedPreferences preferences;

    ColoringTokens(final RedPreferences preferences) {
        this.preferences = preferences;
        this.tokens = new HashMap<>();
    }

    void initialize() {
        tokens.putAll(
                EnumSet.allOf(SyntaxHighlightingCategory.class).stream().collect(toMap(Function.identity(), cat -> {
                    final ColoringPreference coloringPref = preferences.getSyntaxColoring(cat);
                    final boolean isTasksDetectionEnabled = preferences.isTasksDetectionEnabled();
                    final Set<String> taskTags = preferences.getTaskTagsWithPriorities().keySet();
                    return createAttribute(cat, coloringPref, isTasksDetectionEnabled, taskTags);
                })));
    }

    private static Token createAttribute(final SyntaxHighlightingCategory category, final ColoringPreference colorPref,
            final boolean isTasksDetectionEnabled, final Set<String> taskTags) {

        if (category == SyntaxHighlightingCategory.TASKS) {
            return new TodoTaskToken(new TodoTaskTextAttribute(ColorsManager.getColor(colorPref.getRgb()),
                    colorPref.getFontStyle(), isTasksDetectionEnabled, taskTags));
        } else {
            return new Token(
                    new TextAttribute(ColorsManager.getColor(colorPref.getRgb()), null, colorPref.getFontStyle()));
        }
    }

    void refresh(final SyntaxHighlightingCategory category, final ColoringPreference newPref) {
        final Token token = tokens.get(category);

        if (token.getData() instanceof TodoTaskTextAttribute) {
            final TodoTaskTextAttribute oldAttributes = (TodoTaskTextAttribute) token.getData();
            final TodoTaskTextAttribute newAttributes = new TodoTaskTextAttribute(
                    ColorsManager.getColor(newPref.getRgb()), newPref.getFontStyle(), oldAttributes.isEnabled(),
                    oldAttributes.getTags());
            token.setData(newAttributes);
        } else {
            final TextAttribute newAttributes = new TextAttribute(ColorsManager.getColor(newPref.getRgb()), null,
                    newPref.getFontStyle());
            token.setData(newAttributes);
        }
    }

    void refreshTasksAttributes(final boolean isEnabled) {
        final Token token = tokens.get(SyntaxHighlightingCategory.TASKS);

        final TodoTaskTextAttribute oldAttributes = (TodoTaskTextAttribute) token.getData();
        final TodoTaskTextAttribute newAttributes = new TodoTaskTextAttribute(oldAttributes.getForeground(),
                oldAttributes.getStyle(), isEnabled, oldAttributes.getTags());
        token.setData(newAttributes);
    }

    void refreshTasksAttributes(final Set<String> newTags) {
        final Token token = tokens.get(SyntaxHighlightingCategory.TASKS);

        final TodoTaskTextAttribute oldAttributes = (TodoTaskTextAttribute) token.getData();
        final TodoTaskTextAttribute newAttributes = new TodoTaskTextAttribute(oldAttributes.getForeground(),
                oldAttributes.getStyle(), oldAttributes.isEnabled, newTags);
        token.setData(newAttributes);
    }

    Token get(final SyntaxHighlightingCategory category) {
        return tokens.get(category);
    }

    private static class TodoTaskToken extends Token implements ITodoTaskToken {

        private TodoTaskToken(final Object data) {
            super(data);
        }

        @Override
        public boolean isTaskDetectionEnabled() {
            return ((TodoTaskTextAttribute) getData()).isEnabled();
        }

        @Override
        public Pattern getTasksPattern() {
            return ((TodoTaskTextAttribute) getData()).getPattern();
        }
    }

    private static class TodoTaskTextAttribute extends TextAttribute {

        private final boolean isEnabled;

        private final Pattern pattern;

        private final Set<String> tags;

        private TodoTaskTextAttribute(final Color foreground, final int style, final boolean isEnabled,
                final Set<String> tasksTags) {
            super(foreground, null, style);
            this.isEnabled = isEnabled;
            this.tags = ImmutableSet.copyOf(tasksTags);
            this.pattern = Pattern.compile(String.join("|", tasksTags));
        }

        private boolean isEnabled() {
            return isEnabled;
        }

        private Set<String> getTags() {
            return tags;
        }

        private Pattern getPattern() {
            return pattern;
        }
    }
}
