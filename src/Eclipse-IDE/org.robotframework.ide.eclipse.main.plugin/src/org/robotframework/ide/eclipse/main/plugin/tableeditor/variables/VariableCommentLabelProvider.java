package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatcherProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;

class VariableCommentLabelProvider extends MatchesHighlightingLabelProvider {

    VariableCommentLabelProvider(final MatcherProvider matchesProvider) {
        super(matchesProvider);
    }

    @Override
    public final StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        if (!comment.isEmpty()) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(
                    mixStylers(
                            withForeground(RobotTheme.getCommentsColor().getRGB()), 
                            withFontStyle(SWT.ITALIC)));
            final StyledString label = new StyledString("# " + comment, commentStyler);
            return highlightMatches(label, "# ".length(),comment);
        }
        return new StyledString();
    }

    private String getComment(final Object element) {
        return element instanceof RobotVariable ? ((RobotVariable) element).getComment() : "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotVariable) {
            return "# " + getComment(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotVariable) {
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}