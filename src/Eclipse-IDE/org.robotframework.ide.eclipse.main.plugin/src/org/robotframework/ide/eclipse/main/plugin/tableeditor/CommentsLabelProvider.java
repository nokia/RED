package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;

public abstract class CommentsLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public final StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        if (!comment.isEmpty()) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(
                    mixStylers(
                            withForeground(RobotTheme.getCommentsColor().getRGB()), 
                            withFontStyle(SWT.ITALIC)));
            return new StyledString("# " + comment, commentStyler);
        }
        return new StyledString();
    }

    protected abstract String getComment(Object element);

    @Override
    public String getToolTipText(final Object element) {
        return "# " + getComment(element);
    }

    @Override
    public Image getToolTipImage(final Object element) {
        return RobotImages.getTooltipImage().createImage();
    }
}
