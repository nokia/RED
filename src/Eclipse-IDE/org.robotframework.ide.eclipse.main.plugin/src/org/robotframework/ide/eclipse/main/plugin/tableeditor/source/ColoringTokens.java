package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static java.util.stream.Collectors.toMap;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.red.graphics.ColorsManager;

class ColoringTokens {

    private final Map<SyntaxHighlightingCategory, Token> tokens;

    private final RedPreferences preferences;

    ColoringTokens(final RedPreferences preferences) {
        this.preferences = preferences;
        this.tokens = new HashMap<>();
    }

    void initialize() {
        tokens.putAll(EnumSet.allOf(SyntaxHighlightingCategory.class).stream().collect(
                toMap(Function.identity(), cat -> new Token(createAttribute(preferences.getSyntaxColoring(cat))))));
    }

    void refresh(final SyntaxHighlightingCategory category, final ColoringPreference newPref) {
        final Token token = tokens.get(category);
        token.setData(createAttribute(newPref));
    }

    Token get(final SyntaxHighlightingCategory category) {
        return tokens.get(category);
    }

    private TextAttribute createAttribute(final ColoringPreference sectionPref) {
        return new TextAttribute(ColorsManager.getColor(sectionPref.getRgb()), null, sectionPref.getFontStyle());
    }
}
