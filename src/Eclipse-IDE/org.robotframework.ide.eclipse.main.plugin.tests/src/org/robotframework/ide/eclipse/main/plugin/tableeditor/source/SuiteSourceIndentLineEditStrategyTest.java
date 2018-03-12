/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.junit.Before;
import org.junit.Test;

public class SuiteSourceIndentLineEditStrategyTest {

    private final SuiteSourceIndentLineEditStrategy strategy = new SuiteSourceIndentLineEditStrategy(false);

    private Document document;

    private DocumentCommand command;

    @Before
    public void before() {
        document = new Document();
        command = new DocumentCommand() {
        };
    }

    @Test
    public void commandShouldNotBeChanged_whenItIsNotLineBreak() {
        document.set("x");
        command.offset = 1;
        command.text = "q";

        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("q");
    }

    @Test
    public void indentShouldNotBeAdded_whenPreviousLineDoesNotStartFromIndent() {
        document.set("xyz");
        command.offset = 3;
        command.text = "\n";

        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentFromPreviousLineShouldBeAdded() {
        document.set("    abc");
        command.offset = 7;
        command.text = "\n";

        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    ");
    }
}
