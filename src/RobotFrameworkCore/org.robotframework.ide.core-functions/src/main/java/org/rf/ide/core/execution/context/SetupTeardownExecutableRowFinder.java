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
import org.rf.ide.core.testdata.model.table.testCases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author mmarzec
 *
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
        final KeywordContext currentKeywordContext = currentKeywords.get(0);
        if (isKeywordType(RobotDebugExecutionContext.TESTCASE_SETUP_KEYWORD_TYPE, currentKeywordContext)) {
            return extractExecutableRowFromTestCase(currentTestCase.getSetups(), currentModel.getSettingTable()
                    .getTestSetups());
        } else if (isKeywordType(RobotDebugExecutionContext.TESTCASE_TEARDOWN_KEYWORD_TYPE, currentKeywordContext)) {
            return extractExecutableRowFromTestCase(currentTestCase.getTeardowns(), currentModel.getSettingTable()
                    .getTestTeardowns());
        } else if (isKeywordType(RobotDebugExecutionContext.SUITE_SETUP_KEYWORD_TYPE, currentKeywordContext)) {
            return extractExecutableRowFromSettingTable(currentModel.getSettingTable().getSuiteSetups());
        } else if (isKeywordType(RobotDebugExecutionContext.SUITE_TEARDOWN_KEYWORD_TYPE, currentKeywordContext)) {
            return extractExecutableRowFromSettingTable(currentModel.getSettingTable().getSuiteTeardowns());
        }

        return null;
    }

    private RobotExecutableRow<TestCase> extractExecutableRowFromTestCase(
            final List<? extends AKeywordBaseSetting<TestCase>> testCaseSettingsList,
            final List<? extends AKeywordBaseSetting<SettingTable>> settingsList) {
        if (testCaseSettingsList != null && !testCaseSettingsList.isEmpty()) {
            return createSetupExecutableRow(testCaseSettingsList.get(testCaseSettingsList.size() - 1).getKeywordName());
        } else {
            return extractExecutableRowFromSettingTable(settingsList);
        }
    }

    private RobotExecutableRow<TestCase> extractExecutableRowFromSettingTable(
            final List<? extends AKeywordBaseSetting<SettingTable>> settingsList) {
        if (settingsList != null && !settingsList.isEmpty()) {
            return createSetupExecutableRow(settingsList.get(0).getKeywordName());
        }
        return null;
    }

    private RobotExecutableRow<TestCase> createSetupExecutableRow(final RobotToken token) {
        final RobotExecutableRow<TestCase> row = new RobotExecutableRow<TestCase>();
        row.setAction(token);
        return row;
    }

    private boolean isKeywordType(final String keywordType, final KeywordContext currentKeywordContext) {
        return currentKeywordContext.getType().equals(keywordType);
    }

    public void setCurrentTestCase(final TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

    public void setCurrentModel(final RobotFile currentModel) {
        this.currentModel = currentModel;
    }

}
