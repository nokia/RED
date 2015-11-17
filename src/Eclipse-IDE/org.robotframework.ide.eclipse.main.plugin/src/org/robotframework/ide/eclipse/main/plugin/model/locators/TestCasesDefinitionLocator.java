/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class TestCasesDefinitionLocator {

    private final IFile file;

    private final RobotModel model;

    public TestCasesDefinitionLocator(final IFile file) {
        this(file, RedPlugin.getModelManager().getModel());
    }

    public TestCasesDefinitionLocator(final IFile file, final RobotModel model) {
        this.file = file;
        this.model = model;
    }

    public void locateTestCaseDefinition(final TestCaseDetector detector) {
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Optional<RobotCasesSection> section = suiteFile.findSection(RobotCasesSection.class);
        if (!section.isPresent()) {
            return;
        }
        for (final RobotCase testCase : section.get().getChildren()) {
            final ContinueDecision shouldContinue = detector.testCaseDetected(suiteFile, testCase);
            if (shouldContinue == ContinueDecision.STOP) {
                break;
            }
        }
    }

    public interface TestCaseDetector {

        ContinueDecision testCaseDetected(RobotSuiteFile file, RobotCase testCase);

    }
}
