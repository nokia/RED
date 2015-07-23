package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

class KeywordCallNameLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            return new StyledString(keywordCall.getName());
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) element;
            final String name = keywordCall.getName();
            return name.isEmpty() ? "<empty>" : name;
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotKeywordCall) {
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}
