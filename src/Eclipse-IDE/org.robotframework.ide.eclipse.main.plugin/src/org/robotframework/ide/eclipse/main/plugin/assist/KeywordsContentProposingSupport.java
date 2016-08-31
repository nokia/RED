/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class KeywordsContentProposingSupport implements IContentProposingSupport {

    private final RobotSuiteFile suiteFile;
    private final KeywordProposalsLabelProvider labelProvider = new KeywordProposalsLabelProvider();

    public KeywordsContentProposingSupport(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IControlContentAdapter getControlAdapter(final Control control) {
        if (control instanceof Text) {
            return new TextContentAdapter();
        }
        throw new IllegalArgumentException("Unsupported control of type: " + control.getClass().getSimpleName());
    }

    @Override
    public IContentProposalProvider getProposalProvider() {
        return new KeywordProposalsProvider(suiteFile);
    }

    @Override
    public ILabelProvider getLabelProvider() {
        return labelProvider;
    }

    @Override
    public char[] getActivationKeys() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ@$&{".toCharArray();
    }

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getInstance(SWT.CTRL, ' ');
    }
}
