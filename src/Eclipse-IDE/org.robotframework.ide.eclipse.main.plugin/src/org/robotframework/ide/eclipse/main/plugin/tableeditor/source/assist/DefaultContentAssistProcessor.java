/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.TextEditorContextValidator;

/**
 * @author Michal Anglart
 *
 */
public abstract class DefaultContentAssistProcessor implements IContentAssistProcessor {

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new TextEditorContextValidator();
    }

    @Override
    public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {
        return new IContextInformation[0];
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
