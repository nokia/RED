/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteVariableMappingHandler.E4DeleteVariableMappingHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;


/**
 * @author Michal Anglart
 *
 */
public class DeleteVariableMappingHandler extends DIParameterizedHandler<E4DeleteVariableMappingHandler> {

    public DeleteVariableMappingHandler() {
        super(E4DeleteVariableMappingHandler.class);
    }

    public static class E4DeleteVariableMappingHandler {

        @Execute
        public Object deleteMappings(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {
            final List<VariableMapping> mappings = Selections.getElements(selection, VariableMapping.class);
            input.getProjectConfiguration().removeVariableMappings(mappings);

            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_STRUCTURE_CHANGED,
                    input.getProjectConfiguration().getVariableMappings());
            
            return null;
        }
    }
}
