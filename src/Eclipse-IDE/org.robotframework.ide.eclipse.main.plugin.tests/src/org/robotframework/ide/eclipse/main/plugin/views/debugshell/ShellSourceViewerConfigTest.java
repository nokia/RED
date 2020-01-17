/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokenTypeBasedRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableUsageRule;

public class ShellSourceViewerConfigTest {

    @Test
    public void coloringRulesAreCreated() throws Exception {
        final ShellSourceViewerConfig config = new ShellSourceViewerConfig();

        final Map<String, ISyntaxColouringRule[]> coloringRules = config.createColoringRules();

        assertThat(coloringRules).hasSize(1)
                .hasEntrySatisfying(IDocument.DEFAULT_CONTENT_TYPE,
                        rules -> haveExactTypes(rules, TokenTypeBasedRule.class, TokenTypeBasedRule.class,
                                TokenTypeBasedRule.class, ExecutableCallInShellRule.class, VariableUsageRule.class));
    }

    private void haveExactTypes(final ISyntaxColouringRule[] rules, final Class<?>... types) {
        assertThat(rules).hasSameSizeAs(types);
        for (int i = 0; i < rules.length; i++) {
            assertThat(rules[i]).isExactlyInstanceOf(types[i]);
        }
    }
}
