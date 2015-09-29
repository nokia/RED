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
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class TestCasesDefinitionLocator {

    private final IFile file;

    private final boolean useCommonModel;

    public TestCasesDefinitionLocator(final IFile file) {
        this(file, true);
    }

    public TestCasesDefinitionLocator(final IFile file, final boolean useCommonModel) {
        this.file = file;
        this.useCommonModel = useCommonModel;
    }

    public void locateTestCaseDefinition(final TestCaseDetector detector) {
        final RobotSuiteFile suiteFile = getSuiteFile();
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

    private RobotSuiteFile getSuiteFile() {
        return useCommonModel ? RedPlugin.getModelManager().createSuiteFile(file) : new RobotSuiteFile(null, file);
    }

    public interface TestCaseDetector {

        ContinueDecision testCaseDetected(RobotSuiteFile file, RobotCase testCase);

    }
}
