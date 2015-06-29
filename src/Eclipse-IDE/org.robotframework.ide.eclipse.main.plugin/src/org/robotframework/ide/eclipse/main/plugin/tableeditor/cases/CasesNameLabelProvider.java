package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class CasesNameLabelProvider extends StylersDisposingLabelProvider {

    private final Text filteringText;
    private final Styler styler;

    public CasesNameLabelProvider(final Text filter) {
        filteringText = filter;

        styler = addDisposeNeededStyler(new DisposeNeededStyler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.background = new Color(Display.getCurrent(), 255, 255, 175);
                textStyle.borderStyle = SWT.BORDER_DOT;
                markForDisposal(textStyle.background);
            }
        });
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotCase) {
            final String matchingText = filteringText.getText();
            final RobotCase robotCase = (RobotCase) element;
            if (!matchingText.isEmpty()) {
                return createLabelWithMarkedMatches(robotCase, matchingText);
            } else {
                return new StyledString(robotCase.getName());
            }
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return null;
    }

    private StyledString createLabelWithMarkedMatches(final RobotCase robotCase, final String matchingText) {
        final String name = robotCase.getName();
        final int matchLength = matchingText.length();
        
        final StyledString label = new StyledString(name);
        
        int index = name.indexOf(matchingText);

        while (index >= 0) {
            label.setStyle(index, matchLength, styler);
            index = name.indexOf(matchingText, index + 1);
        }
        return label;
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotCase) {
            return RobotImages.getTestCaseImage().createImage();
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }
}
