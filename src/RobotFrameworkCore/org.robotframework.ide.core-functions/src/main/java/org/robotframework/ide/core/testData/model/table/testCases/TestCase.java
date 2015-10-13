/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.presenter.DataDrivenKeywordName;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.RobotTokenPositionComparator;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCase extends AModelElement<TestCaseTable> {

    private RobotToken testName;
    private final List<TestDocumentation> documentation = new LinkedList<>();
    private final List<TestCaseTags> tags = new LinkedList<>();
    private final List<TestCaseSetup> setups = new LinkedList<>();
    private final List<TestCaseTeardown> teardowns = new LinkedList<>();
    private final List<TestCaseTemplate> templates = new LinkedList<>();
    private final List<TestCaseTimeout> timeouts = new LinkedList<>();
    private final List<RobotExecutableRow<TestCase>> testContext = new LinkedList<>();

    private final DataDrivenKeywordName<TestCaseTemplate> templateKeywordGenerator = new DataDrivenKeywordName<>();


    public TestCase(final RobotToken testName) {
        this.testName = testName;
    }


    public RobotToken getTestName() {
        return testName;
    }


    public void setTestName(final RobotToken testName) {
        this.testName = testName;
    }


    public void addTestExecutionRow(
            final RobotExecutableRow<TestCase> executionRow) {
        executionRow.setParent(this);
        this.testContext.add(executionRow);
    }


    public List<RobotExecutableRow<TestCase>> getTestExecutionRows() {
        return Collections.unmodifiableList(testContext);
    }


    public void addDocumentation(final TestDocumentation doc) {
        doc.setParent(this);
        this.documentation.add(doc);
    }


    public List<TestDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentation);
    }


    public void addTag(final TestCaseTags tag) {
        tag.setParent(this);
        tags.add(tag);
    }


    public List<TestCaseTags> getTags() {
        return Collections.unmodifiableList(tags);
    }


    public void addSetup(final TestCaseSetup setup) {
        setup.setParent(this);
        setups.add(setup);
    }


    public List<TestCaseSetup> getSetups() {
        return Collections.unmodifiableList(setups);
    }


    public void addTeardown(final TestCaseTeardown teardown) {
        teardown.setParent(this);
        teardowns.add(teardown);
    }


    public List<TestCaseTeardown> getTeardowns() {
        return Collections.unmodifiableList(teardowns);
    }


    public void addTemplate(final TestCaseTemplate template) {
        template.setParent(this);
        templates.add(template);
    }


    public List<TestCaseTemplate> getTemplates() {
        return Collections.unmodifiableList(templates);
    }


    public void addTimeout(final TestCaseTimeout timeout) {
        timeout.setParent(this);
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

            for (RobotExecutableRow<TestCase> row : testContext) {
                tokens.addAll(row.getElementTokens());
            }

            for (TestCaseTimeout timeout : timeouts) {
                tokens.addAll(timeout.getElementTokens());
            }

            Collections.sort(tokens, new RobotTokenPositionComparator());
        }

        return tokens;
    }


    public boolean isDataDrivenTestCase() {
        return (getTemplateKeywordName() != null);
    }


    public String getTemplateKeywordName() {
        String keywordName = getRobotViewAboutTestTemplate();
        if (keywordName == null) {
            SettingTable settingTable = getParent().getParent()
                    .getSettingTable();
            if (settingTable.isPresent()) {
                keywordName = settingTable.getRobotViewAboutTestTemplate();
                if (keywordName != null && keywordName.isEmpty()) {
                    keywordName = null;
                }
            }
        } else if (keywordName.isEmpty()) {
            keywordName = null;
        }

        return keywordName;
    }


    public String getRobotViewAboutTestTemplate() {
        return templateKeywordGenerator.createRepresentation(templates);
    }
}
