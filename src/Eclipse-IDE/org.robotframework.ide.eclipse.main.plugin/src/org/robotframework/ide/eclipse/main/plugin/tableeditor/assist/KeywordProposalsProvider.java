/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ImportLibraryTableFixer;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

public class KeywordProposalsProvider implements RedContentProposalProvider {

    private final Supplier<RobotSuiteFile> suiteFile;

    public KeywordProposalsProvider(final RobotSuiteFile suiteFile) {
        this(() -> suiteFile);
    }

    public KeywordProposalsProvider(final Supplier<RobotSuiteFile> suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> keywordsEntities = new RedKeywordProposals(suiteFile.get())
                .getKeywordProposals(prefix);

        return keywordsEntities.stream()
                .map(proposal -> new AssistProposalAdapter(proposal,
                        () -> createOperationsToPerformAfterAccepting((RedKeywordProposal) proposal)))
                .toArray(RedContentProposal[]::new);
    }

    private List<Runnable> createOperationsToPerformAfterAccepting(final RedKeywordProposal proposedKeyword) {
        final List<Runnable> operations = new ArrayList<>();
        if (!proposedKeyword.isAccessible()) {
            operations.add(() -> new ImportLibraryTableFixer(proposedKeyword.getSourceName()).apply(suiteFile.get()));
        }
        return operations;
    }
}
