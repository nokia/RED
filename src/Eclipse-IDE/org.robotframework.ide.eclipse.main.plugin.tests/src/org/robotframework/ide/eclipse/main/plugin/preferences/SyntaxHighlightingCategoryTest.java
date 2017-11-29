package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

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
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.section"))
                .isEqualTo(SyntaxHighlightingCategory.SECTION_HEADER);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.setting"))
                .isEqualTo(SyntaxHighlightingCategory.SETTING);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.special"))
                .isEqualTo(SyntaxHighlightingCategory.SPECIAL);
        assertThat(SyntaxHighlightingCategory.fromPreferenceId("red.editor.syntaxColoring.var"))
                .isEqualTo(SyntaxHighlightingCategory.VARIABLE);
    }

}
