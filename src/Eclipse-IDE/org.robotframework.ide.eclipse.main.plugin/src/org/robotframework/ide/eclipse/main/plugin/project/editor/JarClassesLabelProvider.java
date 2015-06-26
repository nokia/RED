package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.JarStructureBuilder.JarClass;

public class JarClassesLabelProvider extends LabelProvider {

    @Override
    public Image getImage(final Object element) {
        return RobotImages.getJavaClassImage().createImage();
    }

    @Override
    public String getText(final Object element) {
        return ((JarClass) element).getQualifiedName();
    }
}
