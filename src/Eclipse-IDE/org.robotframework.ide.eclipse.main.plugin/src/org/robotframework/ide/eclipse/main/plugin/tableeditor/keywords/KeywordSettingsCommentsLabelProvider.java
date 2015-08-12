package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;


public class KeywordSettingsCommentsLabelProvider extends MatchesHighlightingLabelProvider {

    public KeywordSettingsCommentsLabelProvider(final MatchesProvider matchesProvider) {
        super(matchesProvider);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Color getBackground(final Object element) {
        return getSetting(element) == null ? ColorsManager.getColor(250, 250, 250) : null;
    }

    private String getComment(final Object element) {
        final RobotDefinitionSetting setting = getSetting(element);
        return setting != null ? setting.getComment() : "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        if (!comment.isEmpty()) {
            final DisposeNeededStyler commentStyler = addDisposeNeededStyler(
                    mixStylers(withForeground(RedTheme.getCommentsColor().getRGB()), withFontStyle(SWT.ITALIC)));
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
        return ImagesManager.getImage(RedImages.getTooltipImage());
    }

    private RobotDefinitionSetting getSetting(final Object element) {
        return (RobotDefinitionSetting) ((Entry<?, ?>) element).getValue();
    }
}
