/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DataDrivenKeywordName;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.RobotTokenPositionComparator;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCase extends AModelElement<TestCaseTable> implements IExecutableStepsHolder<TestCase> {

    private RobotToken testName;

    private final List<TestDocumentation> documentation = new ArrayList<>();

    private final List<TestCaseTags> tags = new ArrayList<>();

    private final List<TestCaseSetup> setups = new ArrayList<>();

    private final List<TestCaseTeardown> teardowns = new ArrayList<>();

    private final List<TestCaseTemplate> templates = new ArrayList<>();

    private final List<TestCaseTimeout> timeouts = new ArrayList<>();

    private final List<TestCaseUnknownSettings> unknownSettings = new ArrayList<>(0);

    private final List<RobotExecutableRow<TestCase>> testContext = new ArrayList<>();

    private final DataDrivenKeywordName<TestCaseTemplate> templateKeywordGenerator = new DataDrivenKeywordName<>();

    private final MoveElementHelper moveHelper;

    public TestCase(final RobotToken testName) {
        this.testName = testName;
        this.moveHelper = new MoveElementHelper();
    }

    public RobotToken getTestName() {
        return testName;
    }

    public void setTestName(final RobotToken testName) {
        this.testName = testName;
    }

    @Override
    public RobotToken getDeclaration() {
        return getTestName();
    }

    public void addUnknownSettings(final TestCaseUnknownSettings unknownSetting) {
        this.unknownSettings.add(unknownSetting);
    }

    public List<TestCaseUnknownSettings> getUnknownSettings() {
        return Collections.unmodifiableList(unknownSettings);
    }

    public void addTestExecutionRow(final RobotExecutableRow<TestCase> executionRow) {
        executionRow.setParent(this);
        this.testContext.add(executionRow);
    }

    public void addTestExecutionRow(final RobotExecutableRow<TestCase> executionRow, final int position) {
        executionRow.setParent(this);
        this.testContext.set(position, executionRow);
    }

    public void removeExecutableRow(final RobotExecutableRow<TestCase> executionRow) {
        this.testContext.remove(executionRow);
    }

    public boolean moveUpExecutableRow(final RobotExecutableRow<TestCase> executionRow) {
        return moveHelper.moveUp(testContext, executionRow);
    }

    public boolean moveDownExecutableRow(final RobotExecutableRow<TestCase> executionRow) {
        return moveHelper.moveDown(testContext, executionRow);
    }

    public void removeExecutableLineWithIndex(final int rowIndex) {
        this.testContext.remove(rowIndex);
    }

    public void removeAllTestExecutionRows() {
        this.testContext.clear();
    }

    public List<RobotExecutableRow<TestCase>> getTestExecutionRows() {
        return Collections.unmodifiableList(testContext);
    }

    @Override
    public List<RobotExecutableRow<TestCase>> getExecutionContext() {
        return getTestExecutionRows();
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
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            if (getTestName() != null) {
                tokens.add(getTestName());
            }

            for (final TestDocumentation doc : documentation) {
                tokens.addAll(doc.getElementTokens());
            }

            for (final TestCaseSetup setup : setups) {
                tokens.addAll(setup.getElementTokens());
            }

            for (final TestCaseTags tag : tags) {
                tokens.addAll(tag.getElementTokens());
            }

            for (final TestCaseTeardown teardown : teardowns) {
                tokens.addAll(teardown.getElementTokens());
            }

            for (final TestCaseTemplate template : templates) {
                tokens.addAll(template.getElementTokens());
            }

            for (final RobotExecutableRow<TestCase> row : testContext) {
                tokens.addAll(row.getElementTokens());
            }

            for (final TestCaseTimeout timeout : timeouts) {
                tokens.addAll(timeout.getElementTokens());
            }

            Collections.sort(tokens, new RobotTokenPositionComparator());
        }

        return tokens;
    }

    public boolean isDataDrivenTestCase() {
        return (getTemplateKeywordName() != null);
    }

    public RobotToken getTemplateKeywordLocation() {
        RobotToken token = new RobotToken();

        String templateKeyword = getRobotViewAboutTestTemplate();
        if (templateKeyword == null) {
            final SettingTable settingTable = getParent().getParent().getSettingTable();
            if (settingTable.isPresent()) {
                for (final TestTemplate tt : settingTable.getTestTemplates()) {
                    if (tt.getKeywordName() != null) {
                        token = tt.getKeywordName();
                        break;
                    }
                }
            }
        } else {
            for (final TestCaseTemplate tct : templates) {
                if (tct.getKeywordName() != null) {
                    token = tct.getKeywordName();
                    break;
                }
            }
        }

        return token;
    }

    public String getTemplateKeywordName() {
        String keywordName = getRobotViewAboutTestTemplate();
        if (keywordName == null) {
            final SettingTable settingTable = getParent().getParent().getSettingTable();
            if (settingTable.isPresent()) {
                keywordName = settingTable.getRobotViewAboutTestTemplate();
                if (keywordName != null && keywordName.isEmpty()) {
                    keywordName = null;
                }
            }
        } else if (keywordName.isEmpty()) {
            keywordName = null;
        }

        if (keywordName != null && keywordName.equalsIgnoreCase("none")) {
            keywordName = null;
        }

        return keywordName;
    }

    public String getRobotViewAboutTestTemplate() {
        return templateKeywordGenerator.createRepresentation(templates);
    }

    @Override
    public TestCase getHolder() {
        return this;
    }

    @Override
    public List<AModelElement<TestCase>> getUnitSettings() {
        List<AModelElement<TestCase>> settings = new ArrayList<>();
        settings.addAll(getDocumentation());
        settings.addAll(getTags());
        settings.addAll(getSetups());
        settings.addAll(getTeardowns());
        settings.addAll(getTemplates());
        settings.addAll(getTimeouts());
        settings.addAll(getUnknownSettings());

        return settings;
    }
}
