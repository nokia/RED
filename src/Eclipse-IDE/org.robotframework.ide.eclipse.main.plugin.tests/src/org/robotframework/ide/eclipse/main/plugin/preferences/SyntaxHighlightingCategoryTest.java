/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class SyntaxHighlightingCategoryTest {

    @Test
    public void testCreatingCategoryFromPreferenceId() {
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.comment"))
                .isEqualTo(SyntaxHighlightingCategory.COMMENT);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.default"))
                .isEqualTo(SyntaxHighlightingCategory.DEFAULT_SECTION);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.definition"))
                .isEqualTo(SyntaxHighlightingCategory.DEFINITION);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.gherkin"))
                .isEqualTo(SyntaxHighlightingCategory.GHERKIN);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.call"))
                .isEqualTo(SyntaxHighlightingCategory.KEYWORD_CALL);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.quote"))
                .isEqualTo(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.library"))
                .isEqualTo(SyntaxHighlightingCategory.KEYWORD_CALL_LIBRARY);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.section"))
                .isEqualTo(SyntaxHighlightingCategory.SECTION_HEADER);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.setting"))
                .isEqualTo(SyntaxHighlightingCategory.SETTING);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.special"))
                .isEqualTo(SyntaxHighlightingCategory.SPECIAL);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.var"))
                .isEqualTo(SyntaxHighlightingCategory.VARIABLE);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.tasks"))
                .isEqualTo(SyntaxHighlightingCategory.TASKS);
    }

    @Test
    public void testCreatingCategoryFromUnrecognizedPreferenceId() {
        assertThatIllegalStateException().isThrownBy(() -> SyntaxHighlightingCategory.fromPreferenceId("xyz"))
                .withMessage("Unrecognized preference key: xyz")
                .withNoCause();
    }

    @Test
    public void categoriesAreDefinedInDarkPreferenceStyle() throws Exception {
        final String styles = readPreferenceStylesFile("resources/css/dark/preferencestyle.css");

        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            assertThat(styles).containsPattern("\'" + category.getPreferenceId() + "=\\d+,\\d+,\\d+,\\d\'");
        }
    }

    private String readPreferenceStylesFile(final String styleFilePath) throws Exception {
        final IPath path = new Path("/plugin").append(RedPlugin.PLUGIN_ID).append(styleFilePath);
        final URL url = new URI("platform", null, path.toString(), null).toURL();
        try (InputStream in = url.openStream(); InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8)) {
            return CharStreams.toString(reader);
        }
    }

}
