/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;


/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceIndentLineAfterDefinitionStrategy implements IAutoEditStrategy {

    private final boolean isTsvFile;

    public SuiteSourceIndentLineAfterDefinitionStrategy(final boolean isTsv) {
        this.isTsvFile = isTsv;
    }

    @Override
    public void customizeDocumentCommand(final IDocument document, final DocumentCommand command) {
        if (command.length == 0 && command.text != null
                && TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1) {
            autoIndentAfterNewLine(document, command);
        }
    }

    private void autoIndentAfterNewLine(final IDocument document, final DocumentCommand command) {
        if (command.offset == -1 || document.getLength() == 0) {
            return;
        }
        final Set<String> validContentTypes = newHashSet(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final int shift = command.offset;

        try {
            final String contentType = document.getContentType(shift);
            if (validContentTypes.contains(contentType)
                    || (contentType == IDocument.DEFAULT_CONTENT_TYPE && shift > 0 && shift == document.getLength()
                            && validContentTypes.contains(document.getContentType(shift - 1)))) {
                final IRegion lineRegion = document.getLineInformationOfOffset(shift);
                final char lineBegin = document.getChar(lineRegion.getOffset());

                final StringBuffer buf = new StringBuffer(command.text);
                if (!Character.isWhitespace(lineBegin) && lineBegin != '*') {
                    buf.append(getSeparator());
                }
                command.text = buf.toString();
            }
        } catch (final BadLocationException e) {
            // stop work
        }
    }

    protected String getSeparator() {
        return RedPlugin.getDefault().getPreferences().getSeparatorToUse(isTsvFile);
    }
}
