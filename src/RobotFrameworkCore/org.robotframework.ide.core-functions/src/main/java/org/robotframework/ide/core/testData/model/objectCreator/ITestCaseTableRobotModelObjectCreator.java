package org.robotframework.ide.core.testData.model.objectCreator;

import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseSetup;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTeardown;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.model.table.testCases.TestDocumentation;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface ITestCaseTableRobotModelObjectCreator {

    TestCase createTestCase(final RobotToken declaration);


    TestCaseSetup createTestCaseSetup(final RobotToken declaration);


    TestCaseTags createTestCaseTags(final RobotToken declaration);


    TestCaseTeardown createTestCaseTeardown(final RobotToken declaration);


    TestCaseTemplate createTestCaseTemplate(final RobotToken declaration);


    TestCaseTimeout createTestCaseTimeout(final RobotToken declaration);


    TestDocumentation createTestDocumentation(final RobotToken declaration);
}
