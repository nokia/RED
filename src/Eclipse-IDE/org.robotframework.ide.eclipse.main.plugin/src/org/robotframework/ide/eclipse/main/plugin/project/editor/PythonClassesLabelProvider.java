package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.PythonLibStructureBuilder.PythonClass;

class PythonClassesLabelProvider extends LabelProvider {

    private final Image classImage = RobotImages.getJavaClassImage().createImage();

    @Override
    public Image getImage(final Object element) {
        return classImage;
    }

    @Override
    public String getText(final Object element) {
        return ((PythonClass) element).getQualifiedName();
    }

    @Override
    public void dispose() {
        super.dispose();
        classImage.dispose();
    }
}
