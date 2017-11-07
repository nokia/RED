/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.Optional;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

class SuiteSourceInsertLineContinuationStrategy implements IAutoEditStrategy {

    private final boolean isTsvFile;

    public SuiteSourceInsertLineContinuationStrategy(final boolean isTsv) {
        this.isTsvFile = isTsv;
    }

    @Override
    public void customizeDocumentCommand(final IDocument document, final DocumentCommand command) {
        if (command.length == 0 && command.text != null
                && TextUtilities.startsWith(document.getLegalLineDelimiters(), command.text) != -1) {
            insertLineContinuation(document, command);
        }
    }

    private void insertLineContinuation(final IDocument document, final DocumentCommand command) {
        if (command.offset == -1 || document.getLength() == 0) {
            return;
        }

        final Optional<String> lineContinuationIndent = getLineContinuationIndent(document, command);

        final StringBuffer buf = new StringBuffer(command.text);
        if (lineContinuationIndent.isPresent()) {
            buf.append(lineContinuationIndent.get() + getSeparator());
        }
        command.text = buf.toString();
    }

    private Optional<String> getLineContinuationIndent(final IDocument document, final DocumentCommand command) {
        final String commandLineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, command.offset)
                .trim()
                .toLowerCase();
        if (isForLoop(commandLineContent)) {
            return Optional.of("\\");
        }
        if (isDocumentation(commandLineContent)) {
            return Optional.of("...");
        }
        return Optional.empty();
    }

    private boolean isForLoop(final String commandLineContent) {
        return commandLineContent.startsWith(":for") || commandLineContent.startsWith(": for")
                || commandLineContent.startsWith("\\");
    }

    private boolean isDocumentation(final String commandLineContent) {
        return commandLineContent.startsWith("[documentation]") || commandLineContent.startsWith("documentation")
                || commandLineContent.startsWith("...");
    }

    private String getSeparator() {
        return RedPlugin.getDefault().getPreferences().getSeparatorToUse(isTsvFile);
    }
}
