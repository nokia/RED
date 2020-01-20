/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.junit.jupiter.api.Test;

public class RedTemplateContextTypeTest {

    @Test
    public void allGlobalVariableTemplateResolversAreDefined() throws Exception {
        final RedTemplateContextType contextType = new RedTemplateContextType();

        assertThat(contextType.resolvers()).toIterable().hasSize(8);
        assertThat(contextType.resolvers()).toIterable()
                .extracting(TemplateVariableResolver::getType)
                .containsOnly("cursor", "dollar", "user", "date", "year", "time", "word_selection", "line_selection");
    }
}
