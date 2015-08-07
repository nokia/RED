package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCase extends AModelElement {

    private RobotToken testName;
    private final List<TestDocumentation> documentation = new LinkedList<>();
    private final List<TestCaseTags> tags = new LinkedList<>();
    private final List<TestCaseSetup> setups = new LinkedList<>();
    private final List<TestCaseTeardown> teardowns = new LinkedList<>();
    private final List<TestCaseTemplate> templates = new LinkedList<>();
    private final List<TestCaseTimeout> timeouts = new LinkedList<>();
    private final List<RobotExecutableRow> testContext = new LinkedList<>();


    public TestCase(final RobotToken testName) {
        this.testName = testName;
    }


    public RobotToken getTestName() {
        return testName;
    }


    public void setTestName(final RobotToken testName) {
        this.testName = testName;
    }


    public void addTestExecutionRow(final RobotExecutableRow executionRow) {
        this.testContext.add(executionRow);
    }


    public List<RobotExecutableRow> getTestExecutionRows() {
        return testContext;
    }


    public void addDocumentation(final TestDocumentation doc) {
        this.documentation.add(doc);
    }


    public List<TestDocumentation> getDocumentation() {
        return documentation;
    }


    public void addTag(final TestCaseTags tag) {
        tags.add(tag);
    }


    public List<TestCaseTags> getTags() {
        return tags;
    }


    public void addSetup(final TestCaseSetup setup) {
        setups.add(setup);
    }


    public List<TestCaseSetup> getSetups() {
        return setups;
    }


    public void addTeardown(final TestCaseTeardown teardown) {
        teardowns.add(teardown);
    }


    public List<TestCaseTeardown> getTeardowns() {
        return teardowns;
    }


    public void addTemplate(final TestCaseTemplate template) {
        templates.add(template);
    }


    public List<TestCaseTemplate> getTemplates() {
        return templates;
    }


    public void addTimeout(final TestCaseTimeout timeout) {
        timeouts.add(timeout);
    }


    public List<TestCaseTimeout> getTimeouts() {
        return timeouts;
    }


    @Override
    public boolean isPresent() {
        return true;
    }
}
