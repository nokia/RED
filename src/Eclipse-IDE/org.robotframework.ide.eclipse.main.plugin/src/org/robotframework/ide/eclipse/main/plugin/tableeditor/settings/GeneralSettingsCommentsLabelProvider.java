package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatcherProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;

class GeneralSettingsCommentsLabelProvider extends MatchesHighlightingLabelProvider {

    GeneralSettingsCommentsLabelProvider(final MatcherProvider matcherProvider) {
        super(matcherProvider);
    }

    @Override
    public Color getBackground(final Object element) {
        // FIXME : resource leak
        return getSetting(element) == null ? new Color(Display.getDefault(), 250, 250, 250) : null;
    }

    @Override
    public final StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        if (!comment.isEmpty()) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(mixStylers(withForeground(RobotTheme
                    .getCommentsColor().getRGB()), withFontStyle(SWT.ITALIC)));
            final String prefix = "# ";
            return highlightMatches(new StyledString(prefix + comment, commentStyler), prefix.length(), comment);
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        return "# " + getComment(element);
    }

    @Override
    public Image getToolTipImage(final Object element) {
        return RobotImages.getTooltipImage().createImage();
    }

    private String getComment(final Object element) {
        final RobotSetting setting = getSetting(element);
        return setting != null ? setting.getComment() : "";
    }

    private RobotSetting getSetting(final Object element) {
        return (RobotSetting) ((Entry<?, ?>) element).getValue();
    }
}
