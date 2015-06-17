package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;

public class ReservedKeywordProposal extends KeywordProposal {

    public ReservedKeywordProposal(final KeywordSpecification keyword) {
        super(keyword);
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
    public int getPriority() {
        return 0;
    }
}
