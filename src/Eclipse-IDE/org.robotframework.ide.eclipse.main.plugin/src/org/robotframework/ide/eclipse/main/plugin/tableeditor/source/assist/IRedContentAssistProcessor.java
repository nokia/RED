/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

public interface IRedContentAssistProcessor extends IContentAssistProcessor {

    String getProposalsTitle();

    List<String> getApplicableContentTypes();

    boolean isInApplicableContentType(final IDocument document, final int offset) throws BadLocationException;
}
