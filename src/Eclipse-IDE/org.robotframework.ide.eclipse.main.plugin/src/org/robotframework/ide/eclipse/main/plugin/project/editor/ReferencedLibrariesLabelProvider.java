package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class ReferencedLibrariesLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString((String) element);
    }

    @Override
    public Image getImage(final Object element) {
        return RobotImages.getLibraryImage().createImage();
    }

}
