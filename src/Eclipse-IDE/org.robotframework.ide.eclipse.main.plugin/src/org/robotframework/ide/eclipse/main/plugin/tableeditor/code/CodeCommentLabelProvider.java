package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatcherProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;

class CodeCommentLabelProvider extends MatchesHighlightingLabelProvider {

    public CodeCommentLabelProvider(final MatcherProvider matchesProvider) {
        super(matchesProvider);
    }

    @Override
    public final StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        final String prefix = "# ";
        final int matchesHighlightsShift = prefix.length();

        StyledString label = null;
        if (!comment.isEmpty()) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(mixStylers(withForeground(RobotTheme
                    .getCommentsColor().getRGB()), withFontStyle(SWT.ITALIC)));
            label = new StyledString(prefix + comment, commentStyler);
        }
        return highlightMatches(label, matchesHighlightsShift, comment);
    }

    private String getComment(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getComment();
        }
        return "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotElement) {
            return "# " + getComment(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotElement) {
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}