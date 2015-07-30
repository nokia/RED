package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.red.graphics.ImagesManager;

class KeywordProposalsLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public Image getImage(final Object element) {
        return ImagesManager.getImage(((KeywordContentProposal) element).getImage());
    }

    @Override
    public String getText(final Object element) {
        return ((KeywordContentProposal) element).getLabel();
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(((KeywordContentProposal) element).getLabel());
    }
}
