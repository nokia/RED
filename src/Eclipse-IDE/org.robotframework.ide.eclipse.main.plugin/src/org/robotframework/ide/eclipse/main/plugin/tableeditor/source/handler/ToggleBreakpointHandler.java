/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.Set;

import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.ui.ISources;
import org.rf.ide.core.testdata.model.ExecutableLineChecker;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleBreakpointHandler.E4ToggleBreakpointHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

/**
 * @author Michal Anglart
 */
public class ToggleBreakpointHandler extends DIParameterizedHandler<E4ToggleBreakpointHandler> {

    public ToggleBreakpointHandler() {
        super(E4ToggleBreakpointHandler.class);
    }

    public static class E4ToggleBreakpointHandler {

        @Execute
        public void toggleBreakpoint(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Optional @Named(ISources.ACTIVE_MENU_NAME) final Set<String> menuName)
                throws CoreException {

            final SuiteSourceEditor sourceEditor = editor.getSourceEditor();
            int line;
            if (menuName == null || menuName.isEmpty()) {
                // breakpoint was toggled using e.g. keyboard shortcut
                line = sourceEditor.getCurrentLine();
            } else {
                // breakpoint was toggled using ruler
                line = sourceEditor.getLineFromRulerActivity();
            }

            final IFile file = (IFile) sourceEditor.getEditorInput().getAdapter(IResource.class);

            toggle(file, line);
        }

        public static void toggle(final IResource file, final int line) throws CoreException {
            final IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
            for (final IBreakpoint breakpoint : breakpointManager.getBreakpoints()) {
                if (breakpoint.getMarker().getResource().equals(file)
                        && breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1) == line) {
                    breakpoint.delete();
                    return;
                }
            }
            if (isExecutableLine(file, line)) {
                breakpointManager.addBreakpoint(new RobotLineBreakpoint(file, line));
            }
        }

        private static boolean isExecutableLine(final IResource file, final int line) {
            if (file instanceof IFile) {
                final RobotSuiteFile robotSuiteFile = RedPlugin.getModelManager().createSuiteFile((IFile) file);
                if (robotSuiteFile != null) {
                    return ExecutableLineChecker.isExecutableLine(robotSuiteFile.getLinkedElement(), line);
                }
            }
            return false;
        }

    }
}
