/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SortVariablesCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.SortVariablesHandler.E4SortVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class SortVariablesHandler extends DIParameterizedHandler<E4SortVariablesHandler> {

    public SortVariablesHandler() {
        super(E4SortVariablesHandler.class);
    }

    public static class E4SortVariablesHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public Object sortVariables(@Named(RobotEditorSources.SUITE_FILE_MODEL) final RobotSuiteFile file) {
            final Optional<RobotVariablesSection> section = file.findSection(RobotVariablesSection.class);
            if (section.isPresent()) {
                stack.execute(new SortVariablesCommand(section.get()));
            }
            return null;
        }
    }
}
