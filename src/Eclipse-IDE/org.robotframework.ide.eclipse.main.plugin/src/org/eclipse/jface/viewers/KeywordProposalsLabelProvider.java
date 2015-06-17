package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

public class KeywordProposalsLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public Image getImage(final Object element) {
        return ((KeywordProposal) element).getImage();
    }

    @Override
    public String getText(final Object element) {
        return ((KeywordProposal) element).getLabel();
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(((KeywordProposal) element).getLabel());
    }

}
