/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.Collection;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public interface QuickAssistProvider {

    boolean canAssist(IQuickAssistInvocationContext invocationContext);

    Collection<? extends ICompletionProposal> computeQuickAssistProposals(RobotSuiteFile fileModel,
            IQuickAssistInvocationContext invocationContext);

}
