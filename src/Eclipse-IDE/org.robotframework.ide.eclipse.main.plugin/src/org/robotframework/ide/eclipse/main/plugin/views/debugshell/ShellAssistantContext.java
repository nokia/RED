/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotThread;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.InformationControlSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext;


public class ShellAssistantContext extends SuiteSourceAssistantContext {

    public ShellAssistantContext(final InformationControlSupport infoControlSupport,
            final KeySequence activationTrigger) {
        super(infoControlSupport, null, activationTrigger);
    }

    @Override
    public RobotSuiteFile getModel() {
        return DebugShellView.getActiveTarget()
                .map(RobotDebugTarget::getThread)
                .map(RobotThread::getTopStackFrame)
                .flatMap(RobotStackFrame::getPath)
                .flatMap(new RedWorkspace()::fileForUri)
                .map(RedPlugin.getModelManager()::createSuiteFile)
                .orElse(null);
    }

    int getLineNumber() {
        return DebugShellView.getActiveTarget()
                .map(RobotDebugTarget::getThread)
                .map(RobotThread::getTopStackFrame)
                .map(RobotStackFrame::getLineNumber)
                .orElse(0);
    }

    @Override
    public boolean isTsvFile() {
        final RobotSuiteFile model = getModel();
        return model != null && model.isTsvFile();
    }
}
