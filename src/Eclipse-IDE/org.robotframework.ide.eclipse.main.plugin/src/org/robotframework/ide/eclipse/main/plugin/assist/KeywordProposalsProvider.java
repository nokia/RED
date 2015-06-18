package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        final KeywordProposalsFactory proposalsFactory = new KeywordProposalsFactory();
        final List<KeywordProposal> proposals = newArrayList();
        for (final LibrarySpecification spec : importedLibraries) {
            for (final KeywordSpecification keyword : spec.getKeywords()) {
                if (keyword.getName().startsWith(contents.substring(0, position))) {
                    final KeywordProposal proposal = proposalsFactory.create(spec, keyword);
                    proposals.add(proposal);
                }
            }
        }

        Collections.sort(proposals);
        return proposals.toArray(new IContentProposal[0]);
    }

    public Map<String, KeywordSpecification> getKeywordsForCompletionProposals() {
        final List<LibrarySpecification> importedLibraries = suiteFile.getImportedLibraries();

        final Map<String, KeywordSpecification> keywordMap = new HashMap<String, KeywordSpecification>();
        for (final LibrarySpecification spec : importedLibraries) {
            for (final KeywordSpecification keyword : spec.getKeywords()) {
                keywordMap.put(keyword.getName(), keyword);
            }
        }
        return new TreeMap<String, KeywordSpecification>(keywordMap);
    }
}
