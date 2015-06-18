package org.robotframework.ide.eclipse.main.plugin.assist;

import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class KeywordProposalsFactory {

    KeywordProposal create(final LibrarySpecification libSpec, final KeywordSpecification keywordSpec) {
        final String libname = libSpec.getName();

        if ("Reserved".equals(libname)) {
            return new ReservedKeywordProposal(libSpec, keywordSpec);
        } else {
            return new KeywordProposal(libSpec, keywordSpec);
        }
    }
}
