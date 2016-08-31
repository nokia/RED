/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Control;

public interface IContentProposingSupport {

    IControlContentAdapter getControlAdapter(Control control);

    IContentProposalProvider getProposalProvider();

    ILabelProvider getLabelProvider();

    char[] getActivationKeys();

    KeyStroke getKeyStroke();
}
