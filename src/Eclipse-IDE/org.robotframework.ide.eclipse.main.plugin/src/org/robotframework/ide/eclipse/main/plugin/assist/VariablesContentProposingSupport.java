/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


/**
 * @author Michal Anglart
 *
 */
public class VariablesContentProposingSupport implements IContentProposingSupport {

    private final RobotSuiteFile suiteFile;

    private final VariableProposalsLabelProvider labelProvider = new VariableProposalsLabelProvider();

    public VariablesContentProposingSupport(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IControlContentAdapter getControlAdapter(final Control control) {
        if (control instanceof Text) {
            return new VariablesTextContentAdapter();
        }
        throw new IllegalArgumentException("Unsupported control of type: " + control.getClass().getSimpleName());
    }

    @Override
    public IContentProposalProvider getProposalProvider() {
        return new VariableProposalsProvider(suiteFile);
    }

    @Override
    public ILabelProvider getLabelProvider() {
        return labelProvider;
    }

    @Override
    public char[] getActivationKeys() {
        return "@$&%".toCharArray();
    }

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getInstance(SWT.CTRL, ' ');
    }
}
