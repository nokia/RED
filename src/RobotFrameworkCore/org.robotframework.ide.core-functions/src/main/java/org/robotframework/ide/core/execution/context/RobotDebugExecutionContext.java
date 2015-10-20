/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;


/**
 * @author mmarzec
 *
 */
public class RobotDebugExecutionContext {
    
    public static final String MAIN_KEYWORD_TYPE = "Keyword";

    public static final String LOOP_KEYWORD_TYPE = "Test For";

    public static final String LOOP_ITEM_KEYWORD_TYPE = "Test Foritem";

    public static final String TESTCASE_SETUP_KEYWORD_TYPE = "Test Setup";
    
    public static final String TESTCASE_TEARDOWN_KEYWORD_TYPE = "Test Teardown";
    
    public static final String SUITE_SETUP_KEYWORD_TYPE = "Suite Setup";
    
    public static final String SUITE_TEARDOWN_KEYWORD_TYPE = "Suite Teardown";
    
    private RobotFile currentModel;
    
    private LinkedList<KeywordContext> currentKeywords;
    
    private TestCaseExecutionRowCounter testCaseExecutionRowCounter;
    
    private boolean isSetupTeardownKeywordStarted;
    private boolean isForLoopStarted;
    
    public RobotDebugExecutionContext() {
        currentKeywords = new LinkedList<>();
        testCaseExecutionRowCounter = new TestCaseExecutionRowCounter();
    }
    
    public void startSuite(final RobotFileOutput robotFileOutput) {
        currentModel = robotFileOutput.getFileModel();
        ExecutableRowFindersManager.initFindersAtSuiteStart(currentModel, currentModel.getKeywordTable().getKeywords(),
                robotFileOutput.getResourceImportReferences());
    }

    public void startTest(final String testName) {
        if (currentModel != null) {
            final TestCaseTable testCaseTable = currentModel.getTestCaseTable();
            final List<TestCase> testCases = testCaseTable.getTestCases();
            for (final TestCase testCase : testCases) {
                if (testCase.getTestName().getText().toString().equalsIgnoreCase(testName)) {
                    ExecutableRowFindersManager.initFindersAtTestCaseStart(testCase);
                    break;
                }
            }
        }
    }

    public void startKeyword(final String name, final String type, final List<String> arguments) {
        currentKeywords.add(new KeywordContext(name, type));
        checkKeywordType(type);
    }

    public void endKeyword() {
        currentKeywords.removeLast();
    }
    
    public void endTest() {
        testCaseExecutionRowCounter.reset();
    }
    
    public KeywordPosition findKeywordPosition() {
        IRobotExecutableRowFinder executableRowFinder = getExecutableRowFinder();
        RobotExecutableRow<?> executableRow = findExecutableRow(executableRowFinder);
        return createNewKeywordPosition(executableRow);
    }
  
    public IRobotExecutableRowFinder getExecutableRowFinder() {
        IRobotExecutableRowFinder executableRowFinder = null;
        if (isKeywordDirectlyFromTestCase()) {
            if (isSetupTeardownKeywordStarted) {
                executableRowFinder = ExecutableRowFindersManager.provideSetupTeardownExecutableRowFinder();
            } else {
                executableRowFinder = ExecutableRowFindersManager.provideTestCaseExecutableRowFinder(testCaseExecutionRowCounter);
            }
        } else if (isForLoopStarted) {
            executableRowFinder = ExecutableRowFindersManager.provideForLoopExecutableRowFinder(testCaseExecutionRowCounter);
        } else { // keyword from Keywords section or resource file
            executableRowFinder = ExecutableRowFindersManager.provideUserKeywordExecutableRowFinder();
        }
        
        return executableRowFinder;
    }
    
    private RobotExecutableRow<?> findExecutableRow(final IRobotExecutableRowFinder executableRowFinder) {
        RobotExecutableRow<?> executableRow = null;
        if (executableRowFinder != null) {
            executableRow = executableRowFinder.findExecutableRow(currentKeywords);
        }
        return executableRow;
    }
    
    private KeywordPosition createNewKeywordPosition(final RobotExecutableRow<?> executableRow) {

        final KeywordPosition keywordPosition = new KeywordPosition();
        KeywordContext parentKeywordContext = getParentKeywordContext();
        if (parentKeywordContext != null && parentKeywordContext.getResourceImportReference() != null) {
            keywordPosition.setFilePath(parentKeywordContext.getResourceImportReference()
                    .getReference()
                    .getProcessedFile()
                    .getAbsolutePath());
        }
        keywordPosition.setLineNumber(executableRow != null ? executableRow.getAction().getLineNumber() : -1);
        return keywordPosition;
    }
    
    private KeywordContext getParentKeywordContext() {
        KeywordContext parentKeywordContext = null;
        if ((currentKeywords.size() - 2) >= 0) {
            parentKeywordContext = currentKeywords.get(currentKeywords.size() - 2);
        }
        return parentKeywordContext;
    }

    private void checkKeywordType(final String type) {
        if (isForLoopStart(type)) {
            isForLoopStarted = true;
        } else if (isForLoopEnd(type)) {
            isForLoopStarted = false;
            ExecutableRowFindersManager.clearForLoopState();
        } else if (isSetupTeardownEnd(type)) {
            isSetupTeardownKeywordStarted = false;
        } else if (isSetupTeardownStart(type)) {
            isSetupTeardownKeywordStarted = true;
        }
    }
    
    private boolean isForLoopStart(final String keywordType) {
        return keywordType.equalsIgnoreCase(LOOP_ITEM_KEYWORD_TYPE);
    }

    private boolean isForLoopEnd(final String keywordType) {
        return isForLoopStarted && !keywordType.equalsIgnoreCase(LOOP_ITEM_KEYWORD_TYPE);
    }

    private boolean isSetupTeardownStart(final String keywordType) {
        return !keywordType.equalsIgnoreCase(MAIN_KEYWORD_TYPE) && !keywordType.equalsIgnoreCase(LOOP_KEYWORD_TYPE);
    }

    private boolean isSetupTeardownEnd(final String keywordType) {
        return isSetupTeardownKeywordStarted && !keywordType.equalsIgnoreCase(TESTCASE_SETUP_KEYWORD_TYPE)
                && !keywordType.equalsIgnoreCase(TESTCASE_TEARDOWN_KEYWORD_TYPE)
                && !keywordType.equalsIgnoreCase(SUITE_SETUP_KEYWORD_TYPE)
                && !keywordType.equalsIgnoreCase(SUITE_TEARDOWN_KEYWORD_TYPE);
    }
    
    private boolean isKeywordDirectlyFromTestCase() {
        return currentKeywords.size() == 1;
    }

    protected class KeywordContext {

        private String name;
        
        private String type;
        
        private int keywordExecutableRowCounter = 0;
        
        private ResourceImportReference resourceImportReference;
        
        private UserKeyword userKeyword;

        public KeywordContext(final String name, final String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getKeywordExecutableRowCounter() {
            return keywordExecutableRowCounter;
        }

        public void incrementKeywordExecutableRowCounter() {
            keywordExecutableRowCounter++;
        }

        public ResourceImportReference getResourceImportReference() {
            return resourceImportReference;
        }

        public void setResourceImportReference(final ResourceImportReference resourceImportReference) {
            this.resourceImportReference = resourceImportReference;
        }

        public UserKeyword getUserKeyword() {
            return userKeyword;
        }

        public void setUserKeyword(final UserKeyword userKeyword) {
            this.userKeyword = userKeyword;
        }

        public String getType() {
            return type;
        }

    }
    
    public class KeywordPosition {
        private int lineNumber;
        private String filePath;
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public void setLineNumber(final int lineNumber) {
            this.lineNumber = lineNumber;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(final String filePath) {
            this.filePath = filePath;
        }
    }
    
    protected class TestCaseExecutionRowCounter {
        private int counter = 0;
        
        public void increment() {
            counter++;
        }
        
        public void reset() {
            counter = 0;
        }
        
        public int getCounter() {
            return counter;
        }
    }
}
