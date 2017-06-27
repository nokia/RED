/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DataDrivenKeywordName;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCase extends AModelElement<TestCaseTable> implements IExecutableStepsHolder<TestCase>, Serializable {

    private static final long serialVersionUID = -3132511868734109797L;

    private RobotToken testName;

    private final List<AModelElement<TestCase>> allElements = new ArrayList<>();

    public TestCase(final RobotToken testName) {
        this.testName = testName;
        fixForTheType(testName, RobotTokenType.TEST_CASE_NAME, true);
    }

    public RobotToken getTestName() {
        return testName;
    }

    public void setTestName(final RobotToken testName) {
        fixForTheType(testName, RobotTokenType.TEST_CASE_NAME, true);
        this.testName = testName;
    }

    @Override
    public RobotToken getDeclaration() {
        return getTestName();
    }

    public void addElement(final AModelElement<TestCase> element) {
        element.setParent(this);
        allElements.add(element);
    }

    public void addElement(final AModelElement<TestCase> element, final int index) {
        element.setParent(this);
        allElements.add(index, element);
    }

    @Override
    public boolean removeElement(final AModelElement<TestCase> element) {
        return allElements.remove(element);
    }

    public void removeElementAt(final int index) {
        allElements.remove(index);
    }

    public boolean moveElementUp(final AModelElement<TestCase> element) {
        return MoveElementHelper.moveUp(allElements, element);
    }

    public boolean moveElementDown(final AModelElement<TestCase> element) {
        return MoveElementHelper.moveDown(allElements, element);
    }

    public void replaceElement(final AModelElement<TestCase> oldElement, final AModelElement<TestCase> newElement) {
        newElement.setParent(this);
        allElements.set(allElements.indexOf(oldElement), newElement);
    }

    public void removeAllElements() {
        allElements.clear();
    }

    public TestCaseUnknownSettings newUnknownSettings(final int index) {
        final RobotToken dec = RobotToken.create("[]",
                newArrayList(RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION));

        final TestCaseUnknownSettings unknown = new TestCaseUnknownSettings(dec);
        addElement(unknown, index);

        return unknown;
    }

    public List<AModelElement<TestCase>> getAllElements() {
        return Collections.unmodifiableList(allElements);
    }

    @Override
    public List<AModelElement<TestCase>> getElements() {
        return getAllElements();
    }

    @Override
    public List<RobotExecutableRow<TestCase>> getExecutionContext() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW)
                .map(el -> (RobotExecutableRow<TestCase>) el)
                .collect(Collectors.toList());
    }

    public TestDocumentation newDocumentation(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);

        final TestDocumentation testDoc = new TestDocumentation(dec);
        addDocumentation(index, testDoc);

        return testDoc;
    }

    public void addDocumentation(final TestDocumentation doc) {
        addDocumentation(allElements.size(), doc);
    }

    public void addDocumentation(final int index, final TestDocumentation doc) {
        doc.setParent(this);
        allElements.add(index, doc);
        getParent().getParent().getParent().getDocumentationCacher().register(doc);
    }

    public TestCaseTags newTags(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);

        final TestCaseTags testTags = new TestCaseTags(dec);
        addElement(testTags, index);

        return testTags;
    }

    public TestCaseSetup newSetup(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.TEST_CASE_SETTING_SETUP
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.TEST_CASE_SETTING_SETUP);

        final TestCaseSetup testSetup = new TestCaseSetup(dec);
        addElement(testSetup, index);

        return testSetup;
    }

    public TestCaseTeardown newTeardown(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.TEST_CASE_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.TEST_CASE_SETTING_TEARDOWN);

        final TestCaseTeardown testTeardown = new TestCaseTeardown(dec);
        addElement(testTeardown, index);

        return testTeardown;
    }

    public TestCaseTemplate newTemplate(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.TEST_CASE_SETTING_TEMPLATE
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.TEST_CASE_SETTING_TEMPLATE);

        final TestCaseTemplate testTemplate = new TestCaseTemplate(dec);
        addElement(testTemplate, index);

        return testTemplate;
    }

    public TestCaseTimeout newTimeout(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.TEST_CASE_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.TEST_CASE_SETTING_TIMEOUT);

        final TestCaseTimeout testTimeout = new TestCaseTimeout(dec);
        addElement(testTimeout, index);

        return testTimeout;
    }

    public List<TestDocumentation> getDocumentation() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_DOCUMENTATION)
                .map(TestDocumentation.class::cast)
                .collect(Collectors.toList());
    }

    public List<TestCaseTags> getTags() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TAGS)
                .map(TestCaseTags.class::cast)
                .collect(Collectors.toList());
    }

    public List<TestCaseSetup> getSetups() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_SETUP)
                .map(TestCaseSetup.class::cast)
                .collect(Collectors.toList());
    }

    public List<TestCaseTeardown> getTeardowns() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TEARDOWN)
                .map(TestCaseTeardown.class::cast)
                .collect(Collectors.toList());
    }

    public List<TestCaseTemplate> getTemplates() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TEMPLATE)
                .map(TestCaseTemplate.class::cast)
                .collect(Collectors.toList());
    }

    public List<TestCaseTimeout> getTimeouts() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TIMEOUT)
                .map(TestCaseTimeout.class::cast)
                .collect(Collectors.toList());
    }

    public List<TestCaseUnknownSettings> getUnknownSettings() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_SETTING_UNKNOWN)
                .map(TestCaseUnknownSettings.class::cast)
                .collect(Collectors.toList());
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

            for (final AModelElement<TestCase> elem : allElements) {
                tokens.addAll(elem.getElementTokens());
            }
        }
        return tokens;
    }

    public boolean isDataDrivenTestCase() {
        return (getTemplateKeywordName() != null);
    }

    public RobotToken getTemplateKeywordLocation() {
        RobotToken token = new RobotToken();

        final String templateKeyword = getRobotViewAboutTestTemplate();
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
            for (final TestCaseTemplate tct : getTemplates()) {
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
        return DataDrivenKeywordName.createRepresentation(getTemplates());
    }

    @Override
    public TestCase getHolder() {
        return this;
    }

    public boolean isDuplicatedSetting(final AModelElement<TestCase> setting) {
        if (setting.getModelType() == ModelType.TEST_CASE_SETTING_UNKNOWN) {
            return false;
        } else {
            return allElements.stream()
                    .filter(el -> el.getModelType() == setting.getModelType())
                    .collect(Collectors.toList())
                    .indexOf(setting) > 0;
        }
    }

    @Override
    public boolean removeElementToken(final int index) {
        throw new UnsupportedOperationException("This operation is not allowed inside TestCase.");
    }

    @Override
    public RobotToken getName() {
        return getTestName();
    }

    @Override
    public FilePosition getEndPosition() {
        return findEndPosition(getParent().getParent());
    }
}
