/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationsFormatter;


public class DocumentationsFormatterTest {

    @Test
    public void namesInBackticksAreLinkedToHeadersIfTheyExist() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT)))
                .thenReturn("<p>First `Section` and `Second`</p>"
                        + "<h1>Section</h1>");

        final DocumentationsFormatter formatter = new DocumentationsFormatter(env);
        final String formatted = formatter.format(new Documentation(DocFormat.ROBOT, ""));

        assertThat(formatted).contains("<h1 id=\"Section\">Section</h1>");
        assertThat(formatted).contains("<p>First <a href=\"#Section\">Section</a> and `Second`</p>");
    }

    @Test
    public void namesInBackticksAreLinkedToLocalKeywordsIfTheyExist() {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT)))
                .thenReturn("<p>The link to `Kw1` and `Kw2` but not to `Kw3`</p>");

        final DocumentationsFormatter formatter = new DocumentationsFormatter(env);
        final String formatted = formatter.format("", new Documentation(DocFormat.ROBOT, "", newHashSet("Kw1", "Kw2")),
                name -> "link_to_" + name.toLowerCase());

        assertThat(formatted).contains(
                "<p>The link to <a href=\"link_to_kw1\">Kw1</a> and <a href=\"link_to_kw2\">Kw2</a> but not to `Kw3`");

    }

}
