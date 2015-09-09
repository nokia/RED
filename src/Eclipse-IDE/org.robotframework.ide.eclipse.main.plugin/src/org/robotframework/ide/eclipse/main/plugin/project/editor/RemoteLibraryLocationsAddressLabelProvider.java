/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;

class RemoteLibraryLocationsAddressLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final RemoteLocation location = (RemoteLocation) element;
        return new StyledString(location.getUri());
    }
}
