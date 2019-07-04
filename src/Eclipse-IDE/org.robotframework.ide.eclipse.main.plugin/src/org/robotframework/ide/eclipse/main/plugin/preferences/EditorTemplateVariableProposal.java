/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.function.Supplier;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

class EditorTemplateVariableProposal implements ICompletionProposal {

    private final TemplateVariableResolver variable;

    private final int offset;

    private final int length;

    private final Supplier<Shell> shellSupplier;

    private Point selection;


    EditorTemplateVariableProposal(final TemplateVariableResolver variable, final int offset, final int length,
            final Supplier<Shell> shellSupplier) {
        this.variable = variable;
        this.offset = offset;
        this.length = length;
        this.shellSupplier = shellSupplier;
    }

    @Override
    public void apply(final IDocument document) {
        try {
            final String var = variable.getType().equals("dollar") ? "$$" : "${" + variable.getType() + '}';
            document.replace(offset, length, var);
            selection = new Point(offset + var.length(), 0);

        } catch (final BadLocationException e) {
            MessageDialog.openError(shellSupplier.get(), "Error applying template variable proposal", e.getMessage());
        }
    }

    @Override
    public Point getSelection(final IDocument document) {
        return selection;
    }

    @Override
    public String getAdditionalProposalInfo() {
        return variable.getDescription();
    }

    @Override
    public String getDisplayString() {
        return variable.getType();
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
