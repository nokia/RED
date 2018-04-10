/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedImportProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

public class ImportsInCodeProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    public ImportsInCodeProposalsProvider(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {

        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> importProposals = new RedImportProposals(suiteFile)
                .getImportsProposals(prefix);

        final RobotRuntimeEnvironment env = suiteFile.getProject().getRuntimeEnvironment();
        return importProposals.stream().map(proposal -> new AssistProposalAdapter(env, proposal)).toArray(
                RedContentProposal[]::new);
    }
}
