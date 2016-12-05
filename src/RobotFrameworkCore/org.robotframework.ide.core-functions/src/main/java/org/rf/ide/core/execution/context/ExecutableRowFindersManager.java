/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.List;

import org.rf.ide.core.execution.context.RobotDebugExecutionContext.TestCaseExecutionRowCounter;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

/**
 * @author mmarzec
 */
public class ExecutableRowFindersManager {

    public ExecutableRowFindersManager() {
    }

    private RobotParser robotParser;

    private TestCase currentTestCase;

    private RobotFile currentModel;

    private List<UserKeyword> userKeywords;

    private List<ResourceImportReference> resourceImportReferences;

    private ForLoopExecutableRowFinder forLoopExecutableRowFinder;

    private UserKeywordExecutableRowFinder userKeywordExecutableRowFinder;

    private SetupTeardownExecutableRowFinder setupTeardownExecutableRowFinder;

    private TestCaseExecutableRowFinder testCaseExecutableRowFinder;

    public IRobotExecutableRowFinder provideSetupTeardownExecutableRowFinder() {
        if (setupTeardownExecutableRowFinder == null) {
            setupTeardownExecutableRowFinder = new SetupTeardownExecutableRowFinder(currentTestCase, currentModel);
        }
        return setupTeardownExecutableRowFinder;
    }

    public IRobotExecutableRowFinder provideForLoopExecutableRowFinder(
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        if (forLoopExecutableRowFinder == null) {
            forLoopExecutableRowFinder = new ForLoopExecutableRowFinder(currentTestCase, testCaseExecutionRowCounter,
                    provideUserKeywordExecutableRowFinder());
        }
        forLoopExecutableRowFinder.setTestCaseExecutionRowCounter(testCaseExecutionRowCounter);
        return forLoopExecutableRowFinder;
    }

    public IRobotExecutableRowFinder provideUserKeywordExecutableRowFinder() {
        if (userKeywordExecutableRowFinder == null) {
            userKeywordExecutableRowFinder = new UserKeywordExecutableRowFinder(robotParser, userKeywords,
                    resourceImportReferences);
        }
        return userKeywordExecutableRowFinder;
    }

    public IRobotExecutableRowFinder provideTestCaseExecutableRowFinder(
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        if (testCaseExecutableRowFinder == null) {
            testCaseExecutableRowFinder = new TestCaseExecutableRowFinder(currentTestCase, testCaseExecutionRowCounter);
        }
        testCaseExecutableRowFinder.setTestCaseExecutionRowCounter(testCaseExecutionRowCounter);
        return testCaseExecutableRowFinder;
    }

    public void clearForLoopState() {
        if (forLoopExecutableRowFinder != null) {
            forLoopExecutableRowFinder.clear();
        }
    }

    public void clearAtTestCaseEnd() {
        this.currentTestCase = null;
        if (setupTeardownExecutableRowFinder != null) {
            setupTeardownExecutableRowFinder.setCurrentTestCase(null);
        }
    }

    public void initFindersAtSuiteStart(final RobotParser robotParser, final RobotFile currentModel,
            final List<UserKeyword> userKeywords, final List<ResourceImportReference> resourceImportReferences) {
        this.robotParser = robotParser;
        this.currentModel = currentModel;
        this.userKeywords = userKeywords;
        this.resourceImportReferences = resourceImportReferences;
        if (setupTeardownExecutableRowFinder != null) {
            setupTeardownExecutableRowFinder.setCurrentModel(currentModel);
        }
        if (userKeywordExecutableRowFinder != null) {
            userKeywordExecutableRowFinder.setUserKeywords(userKeywords);
            userKeywordExecutableRowFinder.setResourceImportReferences(resourceImportReferences);
            userKeywordExecutableRowFinder.setRobotParser(this.robotParser);
        }
    }

    public void initFindersAtTestCaseStart(final TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
        if (setupTeardownExecutableRowFinder != null) {
            setupTeardownExecutableRowFinder.setCurrentTestCase(currentTestCase);
        }
        if (testCaseExecutableRowFinder != null) {
            testCaseExecutableRowFinder.setCurrentTestCase(currentTestCase);
        }
        if (forLoopExecutableRowFinder != null) {
            forLoopExecutableRowFinder.setCurrentTestCase(currentTestCase);
        }
    }

    public void updateResourceImportReferences(final List<ResourceImportReference> resourceImportReferences) {
        this.resourceImportReferences = resourceImportReferences;
        if (userKeywordExecutableRowFinder != null) {
            userKeywordExecutableRowFinder.setResourceImportReferences(resourceImportReferences);
        }
    }

    public boolean hasCurrentTestCase() {
        return currentTestCase != null;
    }
}
