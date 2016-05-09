/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.List;

import org.rf.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author mmarzec
 */
public class SetupTeardownExecutableRowFinder implements IRobotExecutableRowFinder {

    private TestCase currentTestCase;

    private RobotFile currentModel;

    public SetupTeardownExecutableRowFinder(final TestCase currentTestCase, final RobotFile currentModel) {
        this.currentTestCase = currentTestCase;
        this.currentModel = currentModel;
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final List<KeywordContext> currentKeywords) {
        if (currentModel != null) {
            final KeywordContext currentKeywordContext = currentKeywords.get(0);
            if (isTestCaseSetup(currentKeywordContext)) {
                return extractExecutableRowFromTestCase(currentTestCase.getSetups(),
                        currentModel.getSettingTable().getTestSetups());
            } else if (isTestCaseTeardown(currentKeywordContext)) {
                return extractExecutableRowFromTestCase(currentTestCase.getTeardowns(),
                        currentModel.getSettingTable().getTestTeardowns());
            } else if (isSuiteSetup(currentKeywordContext)) {
                return extractExecutableRowFromSettingTable(currentModel.getSettingTable().getSuiteSetups());
            } else if (isSuiteTeardown(currentKeywordContext)) {
                return extractExecutableRowFromSettingTable(currentModel.getSettingTable().getSuiteTeardowns());
            }
        }
        return null;
    }

    private RobotExecutableRow<TestCase> extractExecutableRowFromTestCase(
            final List<? extends AKeywordBaseSetting<TestCase>> testCaseSettingsList,
            final List<? extends AKeywordBaseSetting<SettingTable>> settingsList) {
        if (testCaseSettingsList != null && !testCaseSettingsList.isEmpty()) {
            return createSetupExecutableRow(testCaseSettingsList.get(testCaseSettingsList.size() - 1).getKeywordName(),
                    testCaseSettingsList.get(0).getParent());
        } else {
            return extractExecutableRowFromSettingTable(settingsList);
        }
    }

    private RobotExecutableRow<TestCase> extractExecutableRowFromSettingTable(
            final List<? extends AKeywordBaseSetting<SettingTable>> settingsList) {
        if (settingsList != null && !settingsList.isEmpty()) {
            TestCase tempCase = new TestCase(null);
            tempCase.setParent(settingsList.get(0).getParent().getParent().getTestCaseTable());
            return createSetupExecutableRow(settingsList.get(0).getKeywordName(), tempCase);
        }
        return null;
    }

    private RobotExecutableRow<TestCase> createSetupExecutableRow(final RobotToken token, final TestCase tc) {
        final RobotExecutableRow<TestCase> row = new RobotExecutableRow<TestCase>();
        row.setAction(token);
        row.setParent(tc);
        return row;
    }

    
    private boolean isSuiteSetup(final KeywordContext currentKeywordContext) {
        final String currentKeywordType = currentKeywordContext.getType();
        return SetupTeardownKeywordTypes.isTypeOf(currentKeywordType, SetupTeardownKeywordTypes.SUITE_SETUP)
                || (currentTestCase == null
                        && SetupTeardownKeywordTypes.isTypeOf(currentKeywordType, SetupTeardownKeywordTypes.NEW_SETUP)); 
    }

    private boolean isSuiteTeardown(final KeywordContext currentKeywordContext) {
        final String currentKeywordType = currentKeywordContext.getType();
        return SetupTeardownKeywordTypes.isTypeOf(currentKeywordType, SetupTeardownKeywordTypes.SUITE_TEARDOWN)
                || (currentTestCase == null && SetupTeardownKeywordTypes.isTypeOf(currentKeywordType,
                        SetupTeardownKeywordTypes.NEW_TEARDOWN));
    }

    private boolean isTestCaseSetup(final KeywordContext currentKeywordContext) {
        final String currentKeywordType = currentKeywordContext.getType();
        return SetupTeardownKeywordTypes.isTypeOf(currentKeywordType, SetupTeardownKeywordTypes.TESTCASE_SETUP)
                || (currentTestCase != null
                        && SetupTeardownKeywordTypes.isTypeOf(currentKeywordType, SetupTeardownKeywordTypes.NEW_SETUP));
    }

    private boolean isTestCaseTeardown(final KeywordContext currentKeywordContext) {
        final String currentKeywordType = currentKeywordContext.getType();
        return SetupTeardownKeywordTypes.isTypeOf(currentKeywordType, SetupTeardownKeywordTypes.TESTCASE_TEARDOWN)
                || (currentTestCase != null && SetupTeardownKeywordTypes.isTypeOf(currentKeywordType,
                        SetupTeardownKeywordTypes.NEW_TEARDOWN));
    }

    public void setCurrentTestCase(final TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

    public void setCurrentModel(final RobotFile currentModel) {
        this.currentModel = currentModel;
    }

    /**
     * New keyword types are common for test case's and suite's setups/teardowns, so additionally
     * presence of TestCase object should be checked.
     * Since Robot 3.0 a2, keywords inside setup/teardown keyword, are carrying main keyword type -
     * 'Keyword'.
     */
    enum SetupTeardownKeywordTypes {
        TESTCASE_SETUP("Test Setup"),
        TESTCASE_TEARDOWN("Test Teardown"),
        SUITE_SETUP("Suite Setup"),
        SUITE_TEARDOWN("Suite Teardown"),
        NEW_SETUP("Setup"), // since Robot 3.0 a2
        NEW_TEARDOWN("Teardown"); // since Robot 3.0 a2 

        private String typeName;

        private SetupTeardownKeywordTypes(final String typeName) {
            this.typeName = typeName;
        }

        private String getTypeName() {
            return typeName;
        }
        
        public static boolean isTypeOf(final String givenTypeName, SetupTeardownKeywordTypes expectedType) {
            return expectedType.getTypeName().equalsIgnoreCase(givenTypeName);
        }
        
        public static boolean isSuiteSetupTeardownType(final String typeName) {
            return SUITE_SETUP.getTypeName().equalsIgnoreCase(typeName) || SUITE_TEARDOWN.getTypeName().equalsIgnoreCase(typeName);
        }
        
        public static boolean isNewSetupTeardownType(final String typeName) {
            return NEW_SETUP.getTypeName().equalsIgnoreCase(typeName) || NEW_TEARDOWN.getTypeName().equalsIgnoreCase(typeName);
        }
    }
}
