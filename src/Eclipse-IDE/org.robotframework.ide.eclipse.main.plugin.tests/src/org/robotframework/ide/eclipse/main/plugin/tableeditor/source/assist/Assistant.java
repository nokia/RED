/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;

class Assistant {

    static SuiteSourceAssistantContext createAssistant(final IFile suite) {
        final RobotModel model = new RobotModel();
        model.createRobotProject(suite.getProject()).setRobotParserComplianceVersion(new RobotVersion(3, 0));
        return createAssistant(model.createSuiteFile(suite));
    }

    static SuiteSourceAssistantContext createAssistant(final RobotSuiteFile model) {
        return new SuiteSourceAssistantContext(null, () -> {
            model.parse();
            return model;
        }, KeySequence.getInstance(KeyStroke.getInstance(SWT.CTRL, SWT.SPACE)),
                new AssistPreferences(new MockRedPreferences("  ")));
    }
}
