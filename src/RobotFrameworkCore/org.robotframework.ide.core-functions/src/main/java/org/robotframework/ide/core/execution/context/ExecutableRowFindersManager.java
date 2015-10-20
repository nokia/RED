/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.List;

import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.TestCaseExecutionRowCounter;
import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;

/**
 * @author mmarzec
 *
 */
public class ExecutableRowFindersManager {

    private ExecutableRowFindersManager() {
    }

    private static TestCase currentTestCase;

    private static RobotFile currentModel;

    private static List<UserKeyword> userKeywords;

    private static List<ResourceImportReference> resourceImportReferences;

    private static ForLoopExecutableRowFinder forLoopExecutableRowFinder;

    private static UserKeywordExecutableRowFinder userKeywordExecutableRowFinder;

    private static SetupTeardownExecutableRowFinder setupTeardownExecutableRowFinder;

    private static TestCaseExecutableRowFinder testCaseExecutableRowFinder;

    public static IRobotExecutableRowFinder provideSetupTeardownExecutableRowFinder() {
        if (setupTeardownExecutableRowFinder == null) {
            setupTeardownExecutableRowFinder = new SetupTeardownExecutableRowFinder(currentTestCase, currentModel);
        }
        return setupTeardownExecutableRowFinder;
    }

    public static IRobotExecutableRowFinder provideForLoopExecutableRowFinder(
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        if (forLoopExecutableRowFinder == null) {
            forLoopExecutableRowFinder = new ForLoopExecutableRowFinder(currentTestCase, testCaseExecutionRowCounter);
        }
        forLoopExecutableRowFinder.setTestCaseExecutionRowCounter(testCaseExecutionRowCounter);
        return forLoopExecutableRowFinder;
    }

    public static IRobotExecutableRowFinder provideUserKeywordExecutableRowFinder() {
        if (userKeywordExecutableRowFinder == null) {
            userKeywordExecutableRowFinder = new UserKeywordExecutableRowFinder(userKeywords, resourceImportReferences);
        }
        return userKeywordExecutableRowFinder;
    }

    public static IRobotExecutableRowFinder provideTestCaseExecutableRowFinder(
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        if (testCaseExecutableRowFinder == null) {
            testCaseExecutableRowFinder = new TestCaseExecutableRowFinder(currentTestCase, testCaseExecutionRowCounter);
        }
        testCaseExecutableRowFinder.setTestCaseExecutionRowCounter(testCaseExecutionRowCounter);
        return testCaseExecutableRowFinder;
    }

    public static void clearForLoopState() {
        forLoopExecutableRowFinder.clear();
    }

    public static void initFindersAtSuiteStart(final RobotFile currentModel, final List<UserKeyword> userKeywords,
            final List<ResourceImportReference> resourceImportReferences) {
        ExecutableRowFindersManager.currentModel = currentModel;
        ExecutableRowFindersManager.userKeywords = userKeywords;
        ExecutableRowFindersManager.resourceImportReferences = resourceImportReferences;
        if (setupTeardownExecutableRowFinder != null) {
            setupTeardownExecutableRowFinder.setCurrentModel(currentModel);
        }
        if (userKeywordExecutableRowFinder != null) {
            userKeywordExecutableRowFinder.setUserKeywords(userKeywords);
            userKeywordExecutableRowFinder.setResourceImportReferences(resourceImportReferences);
        }
    }

    public static void initFindersAtTestCaseStart(final TestCase currentTestCase) {
        ExecutableRowFindersManager.currentTestCase = currentTestCase;
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
}
