/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author Michal Anglart
 *
 */
public class EmptyCompletionProposal implements ICompletionProposal {

    private final String label;

    public EmptyCompletionProposal(final String label) {
        this.label = label;
    }

    @Override
    public void apply(final IDocument document) {
        // nothing to do
    }

    @Override
    public Point getSelection(final IDocument document) {
        return null;
    }

    @Override
    public String getAdditionalProposalInfo() {
        return null;
    }

    @Override
    public String getDisplayString() {
        return label;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

}
