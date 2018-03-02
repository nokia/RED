/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

import java.util.Collection;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public interface RedContentProposal extends IContentProposal {

    ImageDescriptor getImage();

    StyledString getStyledLabel();

    boolean hasDescription();

    ModificationStrategy getModificationStrategy();

    Collection<Runnable> getOperationsToPerformAfterAccepting();

    public interface ModificationStrategy {

        void insert(final Text text, final IContentProposal proposal);

        void insert(final Combo control, final IContentProposal proposal);

        boolean shouldCommitAfterInsert();
    }
}
