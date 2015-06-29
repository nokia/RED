package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedBySourcesAndNames;

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;

class KeywordProposalsProvider implements IContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    KeywordProposalsProvider(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IContentProposal[] getProposals(final String contents, final int position) {
        final String prefix = contents.substring(0, position);
        final List<RedKeywordProposal> keywordsProposals = new RedKeywordProposals(suiteFile).getKeywordProposals(
                prefix, sortedBySourcesAndNames());

        final List<KeywordContentProposal> proposals = newArrayList();
        for (final RedKeywordProposal proposedKeyword : keywordsProposals) {
            proposals.add(new KeywordContentProposal(proposedKeyword));
        }
        return proposals.toArray(new IContentProposal[0]);
    }
}
