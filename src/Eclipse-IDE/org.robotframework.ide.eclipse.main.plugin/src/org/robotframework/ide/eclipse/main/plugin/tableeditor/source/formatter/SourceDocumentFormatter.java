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
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public interface SourceDocumentFormatter {

    public static SourceDocumentFormatter create(final RedPreferences preferences,
            final IRuntimeEnvironment environment) {
        if (preferences.isCustomFormatterUsed()) {
            return new SuiteSourceEditorCustomFormatter(preferences);
        } else {
            return new SuiteSourceEditorTidyFormatter(environment);
        }
    }

    void format(IDocument document) throws BadLocationException;

    void format(IDocument document, List<Integer> changedLines) throws BadLocationException;

    void format(IDocument document, IRegion regionToFormat) throws BadLocationException;

}