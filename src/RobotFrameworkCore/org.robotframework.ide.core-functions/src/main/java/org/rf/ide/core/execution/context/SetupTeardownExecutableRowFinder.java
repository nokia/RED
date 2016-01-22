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

    public static final String TESTCASE_SETUP_KEYWORD_TYPE = "Test Setup";

    public static final String TESTCASE_TEARDOWN_KEYWORD_TYPE = "Test Teardown";

    public static final String SUITE_SETUP_KEYWORD_TYPE = "Suite Setup";

    public static final String SUITE_TEARDOWN_KEYWORD_TYPE = "Suite Teardown";

    public static final String SETUP_KEYWORD_TYPE = "Setup"; // since Robot 3.0 a2

    public static final String TEARDOWN_KEYWORD_TYPE = "Teardown"; // since Robot 3.0 a2

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
            TestCase artCase = new TestCase(null);
            artCase.setParent(settingsList.get(0).getParent().getParent().getTestCaseTable());
            return createSetupExecutableRow(settingsList.get(0).getKeywordName(), artCase);
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
        return currentKeywordContext.getType().equalsIgnoreCase(SUITE_SETUP_KEYWORD_TYPE)
                || (currentTestCase == null && currentKeywordContext.getType().equalsIgnoreCase(SETUP_KEYWORD_TYPE));
    }

    private boolean isSuiteTeardown(final KeywordContext currentKeywordContext) {
        return currentKeywordContext.getType().equalsIgnoreCase(SUITE_TEARDOWN_KEYWORD_TYPE)
                || (currentTestCase == null && currentKeywordContext.getType().equalsIgnoreCase(TEARDOWN_KEYWORD_TYPE));
    }

    private boolean isTestCaseSetup(final KeywordContext currentKeywordContext) {
        return currentKeywordContext.getType().equalsIgnoreCase(TESTCASE_SETUP_KEYWORD_TYPE)
                || (currentTestCase != null && currentKeywordContext.getType().equalsIgnoreCase(SETUP_KEYWORD_TYPE));
    }

    private boolean isTestCaseTeardown(final KeywordContext currentKeywordContext) {
        return currentKeywordContext.getType().equalsIgnoreCase(TESTCASE_TEARDOWN_KEYWORD_TYPE)
                || (currentTestCase != null && currentKeywordContext.getType().equalsIgnoreCase(TEARDOWN_KEYWORD_TYPE));
    }

    public void setCurrentTestCase(final TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

    public void setCurrentModel(final RobotFile currentModel) {
        this.currentModel = currentModel;
    }

}
