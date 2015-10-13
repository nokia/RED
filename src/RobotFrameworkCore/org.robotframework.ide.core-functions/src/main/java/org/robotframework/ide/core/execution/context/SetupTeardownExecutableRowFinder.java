package org.robotframework.ide.core.execution.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;

public class SetupTeardownExecutableRowFinder implements IRobotExecutableRowFinder {

    private TestCase currentTestCase;

    private RobotFile currentModel;

    public SetupTeardownExecutableRowFinder(final TestCase currentTestCase, final RobotFile currentModel) {
        this.currentTestCase = currentTestCase;
        this.currentModel = currentModel;
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final LinkedList<KeywordContext> currentKeywords) {
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
}
