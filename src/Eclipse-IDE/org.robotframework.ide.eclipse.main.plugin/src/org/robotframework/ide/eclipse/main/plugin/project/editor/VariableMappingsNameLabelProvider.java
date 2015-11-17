/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.VariableMapping;

class VariableMappingsNameLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final VariableMapping mapping = (VariableMapping) element;
        return new StyledString(mapping.getName());
    }
}
