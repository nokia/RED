package org.robotframework.ide.core.execution.context;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RobotTestExecutionContext {

    private Map<RobotSuiteExecutionContext, ExecutionStatus> suiteMap;

    private Map<String, Object> currentVariables;

    private LinkedList<RobotTestCaseExecutionContext> currentTestCases;

    private LinkedList<RobotKeywordExecutionContext> currentKeywords;


    public RobotTestExecutionContext() {
        suiteMap = new LinkedHashMap<>();
        currentVariables = new LinkedHashMap<>();
        currentTestCases = new LinkedList<>();
        currentKeywords = new LinkedList<>();
    }


    public void startSuite(String longname) {
        suiteMap.put(new RobotSuiteExecutionContext(longname),
                ExecutionStatus.RUNNING);
    }


    public void startTest(String longname) {
        RobotTestCaseExecutionContext newTestCase = new RobotTestCaseExecutionContext(
                longname);
        String suiteName = longname.substring(0, longname.lastIndexOf("."));
        Set<RobotSuiteExecutionContext> suiteSet = suiteMap.keySet();
        for (RobotSuiteExecutionContext suiteExecutionContext : suiteSet) {
            if (suiteExecutionContext.getLongname().equals(suiteName)) {
                suiteExecutionContext.getTestCaseMap().put(newTestCase,
                        ExecutionStatus.RUNNING);
                break;
            }
        }

        currentTestCases.addLast(newTestCase);
    }


    public void startKeyword(String name, String type, List<String> arguments) {
        RobotKeywordExecutionContext newKeyword = new RobotKeywordExecutionContext(
                name, type, arguments);
        if (!currentKeywords.isEmpty()) {
            RobotKeywordExecutionContext currentKeyword = currentKeywords
                    .getLast();
            currentKeyword.getKeywordMap().put(newKeyword,
                    ExecutionStatus.RUNNING);
        } else {
            RobotTestCaseExecutionContext currentTestCase = currentTestCases
                    .getLast();
            currentTestCase.getKeywordMap().put(newKeyword,
                    ExecutionStatus.RUNNING);
        }

        currentKeywords.addLast(newKeyword);
    }


    public void endKeyword() {
        if (currentKeywords.size() > 1) {
            RobotKeywordExecutionContext parentKeyword = currentKeywords
                    .get(currentKeywords.size() - 2);
            parentKeyword.getKeywordMap().put(currentKeywords.getLast(),
                    ExecutionStatus.EXECUTED);
        } else {
            RobotTestCaseExecutionContext currentTestCase = currentTestCases
                    .getLast();
            currentTestCase.getKeywordMap().put(currentKeywords.getLast(),
                    ExecutionStatus.EXECUTED);
        }

        currentKeywords.removeLast();
    }


    public void endTest(String longname) {
        String suiteName = longname.substring(0, longname.lastIndexOf("."));
        Set<RobotSuiteExecutionContext> suiteSet = suiteMap.keySet();
        for (RobotSuiteExecutionContext suiteExecutionContext : suiteSet) {
            if (suiteExecutionContext.getLongname().equals(suiteName)) {
                Set<RobotTestCaseExecutionContext> testCaseSet = suiteExecutionContext
                        .getTestCaseMap().keySet();
                for (RobotTestCaseExecutionContext testCaseExecutionContext : testCaseSet) {
                    if (testCaseExecutionContext.getLongname().equals(longname)) {
                        suiteExecutionContext.getTestCaseMap().put(
                                testCaseExecutionContext,
                                ExecutionStatus.EXECUTED);
                        break;
                    }
                }
            }
        }

        currentTestCases.removeLast();
    }


    public void endSuite(String longname) {
        Set<RobotSuiteExecutionContext> suiteSet = suiteMap.keySet();
        for (RobotSuiteExecutionContext suiteExecutionContext : suiteSet) {
            if (suiteExecutionContext.getLongname().equals(longname)) {
                suiteMap.put(suiteExecutionContext, ExecutionStatus.EXECUTED);
                break;
            }
        }
    }


    public Map<RobotSuiteExecutionContext, ExecutionStatus> getSuiteMap() {
        return suiteMap;
    }


    public void setCurrentVariables(Map<String, Object> currentVariables) {
        this.currentVariables = currentVariables;
    }


    public Map<String, Object> getCurrentTestVariables() {
        return currentVariables;
    }
}
