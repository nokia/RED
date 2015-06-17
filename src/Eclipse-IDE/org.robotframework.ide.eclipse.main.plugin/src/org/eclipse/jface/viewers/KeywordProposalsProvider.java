package org.eclipse.jface.viewers;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public class KeywordProposalsProvider implements IContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    public KeywordProposalsProvider(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IContentProposal[] getProposals(final String contents, final int position) {
        final List<LibrarySpecification> importedLibraries = suiteFile.getImportedLibraries();

        final List<KeywordProposal> proposals = newArrayList();
        for (final LibrarySpecification spec : importedLibraries) {
            for (final KeywordSpecification keyword : spec.getKeywords()) {
                if (keyword.getName().startsWith(contents.substring(0, position))) {
                    final KeywordProposal proposal = spec.getName().equals("Reserved") ? new ReservedKeywordProposal(
                            keyword) : new KeywordProposal(keyword);
                    proposals.add(proposal);
                }
            }
        }

        // TODO : go through keyword defined in the file itself and other
        // imported resources

        Collections.sort(proposals, withProposalsComparator());
        return proposals.toArray(new IContentProposal[0]);
    }

    private static Comparator<KeywordProposal> withProposalsComparator() {
        return new Comparator<KeywordProposal>() {
            @Override
            public int compare(final KeywordProposal proposal1, final KeywordProposal proposal2) {
                final int result = Integer.compare(proposal1.getPriority(), proposal2.getPriority());
                if (result != 0) {
                    return result;
                }
                return proposal1.getLabel().compareTo(proposal2.getLabel());
            }
        };
    }
}
