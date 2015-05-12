package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotLibrary;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;

public class NavigatorLibrariesLabelProvider implements ILabelProvider, IStyledLabelProvider {

    @Override
    public void addListener(final ILabelProviderListener listener) {
        // nothing to do
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
        // nothing to do
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return RobotImages.getLibraryImage().createImage();
        } else if (element instanceof RobotLibrary) {
            return RobotImages.getBookImage().createImage();
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return "Robot Standard libraries";
        } else if (element instanceof RobotLibrary) {
            return ((RobotLibrary) element).getName();
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            final RobotProjectDependencies dependencies = (RobotProjectDependencies) element;

            final StyledString styled = new StyledString("Robot Standard libraries");
            styled.append(" ");
            styled.append("[" + dependencies.getVersion() + "]", new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RobotTheme.getEclipseDecorationColor();
                }
            });
            return styled;
        } else if (element instanceof RobotLibrary) {
            return new StyledString(((RobotLibrary) element).getName());
        }
        return new StyledString();
    }
}
