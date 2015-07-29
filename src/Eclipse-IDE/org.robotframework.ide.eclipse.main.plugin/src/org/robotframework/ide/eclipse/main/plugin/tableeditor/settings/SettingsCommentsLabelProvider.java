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

class SettingsCommentsLabelProvider extends MatchesHighlightingLabelProvider {

    public SettingsCommentsLabelProvider(final MatcherProvider matchesProvider) {
        super(matchesProvider);
    }

    private String getComment(final Object element) {
        final RobotSetting setting = getSetting(element);
        return setting != null ? setting.getComment() : "";
    }

    private RobotSetting getSetting(final Object element) {
        if (element instanceof RobotSetting) {
            return (RobotSetting) element;
        } else if (element instanceof Entry<?, ?>) {
            return (RobotSetting) ((Entry<?, ?>) element).getValue();
        }
        return null;
    }

    @Override
    public final StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        if (!comment.isEmpty()) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(mixStylers(withForeground(RobotTheme
                    .getCommentsColor().getRGB()), withFontStyle(SWT.ITALIC)));
            String prefix = "# ";
            return highlightMatches(new StyledString(prefix + comment, commentStyler), prefix.length(), comment);
        }
        return new StyledString();
    }

    @Override
    public Color getBackground(final Object element) {
        if (element instanceof Entry<?, ?> && ((Entry<?, ?>) element).getValue() == null) {
            return new Color(Display.getDefault(), 250, 250, 250);
        } else {
            return null;
        }
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotSetting || element instanceof Entry<?, ?>) {
            return "# " + getComment(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotSetting || element instanceof Entry<?, ?>) {
            return RobotImages.getTooltipImage().createImage();
        }
        return null;
    }
}
