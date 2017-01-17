/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;

import com.google.common.base.Supplier;

class Assistant {

    static SuiteSourceAssistantContext createAssistant(final IFile suite) {
        return createAssistant(new RobotModel().createSuiteFile(suite));
    }

    static SuiteSourceAssistantContext createAssistant(final RobotSuiteFile model) {
        return new SuiteSourceAssistantContext(new Supplier<RobotSuiteFile>() {

            @Override
            public RobotSuiteFile get() {
                model.parse();
                return model;
            }
        }, new AssistPreferences(new MockRedPreferences(false, "  ")));
    }
}
