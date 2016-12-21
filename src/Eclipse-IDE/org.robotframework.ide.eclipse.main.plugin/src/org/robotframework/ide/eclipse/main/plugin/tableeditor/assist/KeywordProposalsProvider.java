/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposal.ModificationStrategy;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.jface.assist.RedTextContentAdapter.SubstituteTextModificationStrategy;

import com.google.common.base.Optional;

public class KeywordProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    public KeywordProposalsProvider(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> keywordsEntities = new RedKeywordProposals(suiteFile)
                .getKeywordProposals(prefix);

        final List<IContentProposal> proposals = newArrayList();
        for (final AssistProposal proposedKeyword : keywordsEntities) {
            proposals.add(new AssistProposalAdapter(proposedKeyword,
                    Optional.<ModificationStrategy> of(new SubstituteTextModificationStrategy())));
        }
        return proposals.toArray(new RedContentProposal[0]);
    }
}
