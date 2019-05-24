/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.testdata.formatter.TidyFormatter;

class SuiteSourceEditorTidyFormatter implements SourceDocumentFormatter {

    private final IRuntimeEnvironment environment;

    public SuiteSourceEditorTidyFormatter(final IRuntimeEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void format(final IDocument document) throws BadLocationException {
        final String content = document.get();

        final TidyFormatter formatter = new TidyFormatter(environment);
        final String formattedContent = formatter.format(content);
        if (!content.equals(formattedContent)) {
            document.set(formattedContent);
        }
    }

    @Override
    public void format(final IDocument document, final List<Integer> changedLines) throws BadLocationException {
        format(document);
    }

    @Override
    public void format(final IDocument document, final IRegion regionToFormat) throws BadLocationException {
        format(document);
    }
}
