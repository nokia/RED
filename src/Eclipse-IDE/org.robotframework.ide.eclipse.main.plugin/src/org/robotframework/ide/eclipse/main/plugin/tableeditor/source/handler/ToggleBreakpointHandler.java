/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.List;
import java.util.Optional;
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
import org.eclipse.ui.ISources;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.ToggleBreakpointHandler.E4ToggleBreakpointHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.annotations.VisibleForTesting;

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
                @org.eclipse.e4.core.di.annotations.Optional @Named(ISources.ACTIVE_MENU_NAME) final Set<String> menuName)
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
            final Optional<Integer> targetBreakpointLine = getPossibleBreakpointLine(file, line);

            final Optional<IBreakpoint> existingBreakpoint = targetBreakpointLine
                    .flatMap(l -> findBreakpoint(breakpointManager, file, l));
            if (existingBreakpoint.isPresent()) {
                existingBreakpoint.get().delete();

            } else if (targetBreakpointLine.isPresent()) {
                breakpointManager.addBreakpoint(new RobotLineBreakpoint(file, targetBreakpointLine.get()));
            }
        }

        private static Optional<IBreakpoint> findBreakpoint(final IBreakpointManager manager, final IResource file,
                final int line) {
            for (final IBreakpoint breakpoint : manager.getBreakpoints()) {
                if (breakpoint.getMarker().getResource().equals(file)
                        && breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1) == line) {
                    return Optional.of(breakpoint);
                }
            }
            return Optional.empty();
        }

        private static Optional<Integer> getPossibleBreakpointLine(final IResource file, final int lineOfToggle) {
            return Optional.ofNullable(file)
                    .filter(IFile.class::isInstance)
                    .map(IFile.class::cast)
                    .map(f -> RedPlugin.getModelManager().createSuiteFile(f))
                    .flatMap(m -> getPossibleBreakpointLine(m, lineOfToggle));
        }

        @VisibleForTesting
        static Optional<Integer> getPossibleBreakpointLine(final RobotSuiteFile robotSuiteFile,
                final int lineOfToggle) {
            final RobotFile model = robotSuiteFile.getLinkedElement();
            return getFirstTokenOffset(model, lineOfToggle).flatMap(robotSuiteFile::findElement)
                    .filter(RobotFileInternalElement.class::isInstance)
                    .map(RobotFileInternalElement.class::cast)
                    .map(RobotFileInternalElement::getLinkedElement)
                    .filter(AModelElement.class::isInstance)
                    .map(AModelElement.class::cast)
                    .filter(E4ToggleBreakpointHandler::canPlaceLineBreakpointOn)
                    .map(AModelElement::getDeclaration)
                    .map(RobotToken::getLineNumber);
        }

        private static Optional<Integer> getFirstTokenOffset(final RobotFile model, final int line) {
            final List<RobotLine> content = model.getFileContent();
            return 0 <= line - 1 && line - 1 < content.size()
                    ? content.get(line - 1).tokensStream().findFirst().map(RobotToken::getStartOffset)
                    : Optional.empty();
        }

        private static boolean canPlaceLineBreakpointOn(final AModelElement<?> modelElem) {
            return modelElem instanceof RobotExecutableRow<?>
                    || modelElem instanceof ExecutableSetting
                    || modelElem instanceof LocalSetting<?>
                            && ((LocalSetting<?>) modelElem).adaptTo(ExecutableSetting.class) != null;
        }
    }
}
