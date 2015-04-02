package org.robotframework.ide.eclipse.main.plugin.explorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.robotframework.ide.eclipse.main.plugin.nature.RobotProjectNature;

public class SuiteInitFilesFilter extends ViewerFilter {

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof IFile) {
            return !(RobotProjectNature.isRobotSuiteInitializationFile((IFile) element));
        }
        return true;
    }

}
