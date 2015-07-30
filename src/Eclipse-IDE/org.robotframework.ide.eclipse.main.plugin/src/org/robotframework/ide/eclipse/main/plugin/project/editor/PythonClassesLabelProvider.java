package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.editor.PythonLibStructureBuilder.PythonClass;
import org.robotframework.red.graphics.ImagesManager;

class PythonClassesLabelProvider extends LabelProvider {

    @Override
    public Image getImage(final Object element) {
        return ImagesManager.getImage(RedImages.getJavaClassImage());
    }

    @Override
    public String getText(final Object element) {
        return ((PythonClass) element).getQualifiedName();
    }
}
