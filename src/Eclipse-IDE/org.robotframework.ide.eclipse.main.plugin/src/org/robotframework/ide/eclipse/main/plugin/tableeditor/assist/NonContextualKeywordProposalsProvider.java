/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;
import java.util.function.Supplier;

import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

public class NonContextualKeywordProposalsProvider implements RedContentProposalProvider {

    private final Supplier<RobotSuiteFile> suiteFile;

    public NonContextualKeywordProposalsProvider(final Supplier<RobotSuiteFile> suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> keywordsProposals = new RedKeywordProposals(suiteFile.get())
                .getKeywordProposals(prefix);

        final IRuntimeEnvironment env = suiteFile.get().getRobotProject().getRuntimeEnvironment();
        return keywordsProposals.stream()
                .map(proposal -> new AssistProposalAdapter(env, proposal))
                .toArray(RedContentProposal[]::new);
    }

}
