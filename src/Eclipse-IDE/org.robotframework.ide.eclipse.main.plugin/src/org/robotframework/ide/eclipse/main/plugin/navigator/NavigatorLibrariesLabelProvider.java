package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public class NavigatorLibrariesLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    private Image libraryImage = RobotImages.getLibraryImage().createImage();
    private Image bookImage = RobotImages.getBookImage().createImage();

    @Override
    public void dispose() {
        super.dispose();
        if (libraryImage != null) {
            libraryImage.dispose();
            libraryImage = null;
        }
        if (bookImage != null) {
            bookImage.dispose();
            bookImage = null;
        }
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return libraryImage;
        } else if (element instanceof LibrarySpecification) {
            return bookImage;
        }
        return null;
    }

    @Override
    public String getText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            return "Robot Standard libraries";
        } else if (element instanceof LibrarySpecification) {
            return ((LibrarySpecification) element).getName();
        }
        return "";
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotProjectDependencies) {
            final RobotProjectDependencies dependencies = (RobotProjectDependencies) element;

            final StyledString styled = new StyledString(dependencies.getName());
            final String additionalInfo = dependencies.getAdditionalInformation();
            if (!additionalInfo.isEmpty()) {
                styled.append(" ");
            }
            return styled.append(additionalInfo, new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RobotTheme.getEclipseDecorationColor();
                }
            });
        } else if (element instanceof LibrarySpecification) {
            final LibrarySpecification libSpec = (LibrarySpecification) element;
            final StyledString styled = new StyledString(libSpec.getName());
            final String additonalInfo = libSpec.getAdditionalInformation();
            if (!additonalInfo.isEmpty()) {
                styled.append(" ");
                styled.append(additonalInfo, new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RobotTheme.getEclipseDecorationColor();
                }
                });
            }
            styled.append(" ");
            return styled.append("(" + libSpec.getKeywords().size() + ")", new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RobotTheme.getEclipseDecorationColor();
                }
            });
        }
        return new StyledString();
    }
}
