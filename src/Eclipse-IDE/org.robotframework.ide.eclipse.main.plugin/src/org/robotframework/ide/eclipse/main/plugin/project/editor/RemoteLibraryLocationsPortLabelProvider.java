package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;

class RemoteLibraryLocationsPortLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final RemoteLocation location = (RemoteLocation) element;
        return new StyledString(Integer.toString(location.getPort()));
    }
}
