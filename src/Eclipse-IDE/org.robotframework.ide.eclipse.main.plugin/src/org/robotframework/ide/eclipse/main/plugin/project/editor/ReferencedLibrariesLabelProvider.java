package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;

public class ReferencedLibrariesLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final ReferencedLibrary lib = (ReferencedLibrary) element;

        if (lib.provideType() == LibraryType.JAVA) {
            final StyledString label = new StyledString(lib.getName());
            label.append(" " + lib.getPath(), new Styler() {
                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.foreground = RobotTheme.getEclipseDecorationColor();
                }
            });
            return label;
        } else {
            return new StyledString(lib.getPath());
        }
    }

    @Override
    public Image getImage(final Object element) {
        final ReferencedLibrary library = (ReferencedLibrary) element;
        return library.getImage().createImage();
    }
}
