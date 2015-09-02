package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.RobotTokenPositionComparator;
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
        executionRow.setFileUUID(getFileUUID());
        this.testContext.add(executionRow);
    }


    public List<RobotExecutableRow> getTestExecutionRows() {
        return Collections.unmodifiableList(testContext);
    }


    public void addDocumentation(final TestDocumentation doc) {
        this.documentation.add(doc);
    }


    public List<TestDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentation);
    }


    public void addTag(final TestCaseTags tag) {
        tag.setFileUUID(getFileUUID());
        tags.add(tag);
    }


    public List<TestCaseTags> getTags() {
        return Collections.unmodifiableList(tags);
    }


    public void addSetup(final TestCaseSetup setup) {
        setup.setFileUUID(getFileUUID());
        setups.add(setup);
    }


    public List<TestCaseSetup> getSetups() {
        return Collections.unmodifiableList(setups);
    }


    public void addTeardown(final TestCaseTeardown teardown) {
        teardown.setFileUUID(getFileUUID());
        teardowns.add(teardown);
    }


    public List<TestCaseTeardown> getTeardowns() {
        return Collections.unmodifiableList(teardowns);
    }


    public void addTemplate(final TestCaseTemplate template) {
        template.setFileUUID(getFileUUID());
        templates.add(template);
    }


    public List<TestCaseTemplate> getTemplates() {
        return Collections.unmodifiableList(templates);
    }


    public void addTimeout(final TestCaseTimeout timeout) {
        timeout.setFileUUID(getFileUUID());
        timeouts.add(timeout);
    }


    public List<TestCaseTimeout> getTimeouts() {
        return Collections.unmodifiableList(timeouts);
    }


    @Override
    public boolean isPresent() {
        return (getTestName() != null);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getTestName().getFilePosition();
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            if (getTestName() != null) {
                tokens.add(getTestName());
            }

            for (TestDocumentation doc : documentation) {
                tokens.addAll(doc.getElementTokens());
            }

            for (TestCaseSetup setup : setups) {
                tokens.addAll(setup.getElementTokens());
            }

            for (TestCaseTags tag : tags) {
                tokens.addAll(tag.getElementTokens());
            }

            for (TestCaseTeardown teardown : teardowns) {
                tokens.addAll(teardown.getElementTokens());
            }

            for (TestCaseTemplate template : templates) {
                tokens.addAll(template.getElementTokens());
            }

            for (RobotExecutableRow row : testContext) {
                tokens.addAll(row.getElementTokens());
            }

            for (TestCaseTimeout timeout : timeouts) {
                tokens.addAll(timeout.getElementTokens());
            }

            Collections.sort(tokens, new RobotTokenPositionComparator());
        }

        return tokens;
    }
}
