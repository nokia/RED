package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class UserKeywordNamesLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            final DisposeNeededStyler styler = addDisposeNeededStyler(Stylers.withFontStyle(SWT.BOLD));
            return new StyledString(((RobotKeywordDefinition) element).getName(), styler);
        } else if (element instanceof RobotKeywordCall) {
            return new StyledString(((RobotKeywordCall) element).getName());
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return null;
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            return RobotImages.getUserKeywordImage().createImage();
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getName();
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotKeywordDefinition || element instanceof RobotKeywordCall) {
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}
