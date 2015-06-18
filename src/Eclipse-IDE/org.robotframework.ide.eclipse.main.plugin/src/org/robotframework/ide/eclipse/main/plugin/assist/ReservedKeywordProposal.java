package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

class ReservedKeywordProposal extends KeywordProposal {

    ReservedKeywordProposal(final LibrarySpecification libSpec, final KeywordSpecification keyword) {
        super(libSpec, keyword);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public boolean hasDescription() {
        return false;
    }

    @Override
    public int compareTo(final KeywordProposal other) {
        if (other instanceof ReservedKeywordProposal) {
            return getName().compareTo(other.getName());
        }
        return -1;
    }
}
