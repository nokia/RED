/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.base.Optional;

class CaseSettingsContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        // nothing to do
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        @SuppressWarnings("unchecked")
        final Optional<RobotCase> testCase = (Optional<RobotCase>) inputElement;
        final Map<String, RobotElement> keywordSettings = CaseSettingsModel
                .buildCaseSettingsMapping(testCase.orNull());
        return keywordSettings.entrySet().toArray();
    }

}
