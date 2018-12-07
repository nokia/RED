/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.IRegionCacheable;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.CommonCase;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Strings;

public class TestCase extends CommonCase<TestCaseTable, TestCase> implements Serializable {

    private static final long serialVersionUID = -3132511868734109797L;

    private RobotToken testName;

    private final List<AModelElement<TestCase>> allElements = new ArrayList<>();

    public TestCase(final RobotToken testName) {
        this.testName = testName;
        fixForTheType(testName, RobotTokenType.TEST_CASE_NAME, true);
    }

    private RobotVersion getVersion() {
        return getParent().getParent().getParent().getRobotVersion();
    }

    @Override
    public RobotToken getName() {
        return testName;
    }

    @Override
    public void setName(final RobotToken testName) {
        fixForTheType(testName, RobotTokenType.TEST_CASE_NAME, true);
        this.testName = testName;
    }

    @Override
    public RobotToken getDeclaration() {
        return getName();
    }

    public AModelElement<TestCase> addElement(final AModelElement<?> element) {
        return addElement(allElements.size(), element);
    }

    public AModelElement<TestCase> addElement(final int index, final AModelElement<?> element) {
        @SuppressWarnings("unchecked")
        final AModelElement<TestCase> elementCasted = (AModelElement<TestCase>) element;
        elementCasted.setParent(this);
        allElements.add(index, elementCasted);

        if (element.getModelType() == ModelType.TEST_CASE_DOCUMENTATION) {
            final IRegionCacheable<IDocumentationHolder> adapter = ((LocalSetting<TestCase>) elementCasted)
                    .adaptTo(IDocumentationHolder.class);
            getParent().getParent().getParent().getDocumentationCacher().register(adapter);
        }
        return elementCasted;
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

    public LocalSetting<TestCase> newUnknownSetting(final int index) {
        return newUnknownSetting(index, "[]");
    }

    public LocalSetting<TestCase> newUnknownSetting(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_SETTING_UNKNOWN, dec));
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
        return executablesStream().collect(toList());
    }

    public LocalSetting<TestCase> newDocumentation(final int index) {
        final String representation = RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newDocumentation(index, representation);
    }

    public LocalSetting<TestCase> newDocumentation(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_DOCUMENTATION, dec));
    }

    public LocalSetting<TestCase> newTags(final int index) {
        final String representation = RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTags(index, representation);
    }

    public LocalSetting<TestCase> newTags(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_TAGS, dec));
    }

    public LocalSetting<TestCase> newSetup(final int index) {
        final String representation = RobotTokenType.TEST_CASE_SETTING_SETUP
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newSetup(index, representation);
    }

    public LocalSetting<TestCase> newSetup(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_SETUP, dec));
    }

    public LocalSetting<TestCase> newTeardown(final int index) {
        final String representation = RobotTokenType.TEST_CASE_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTeardown(index, representation);
    }

    public LocalSetting<TestCase> newTeardown(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_TEARDOWN, dec));
    }

    public LocalSetting<TestCase> newTemplate(final int index) {
        final String representation = RobotTokenType.TEST_CASE_SETTING_TEMPLATE
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTemplate(index, representation);
    }

    public LocalSetting<TestCase> newTemplate(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TEST_CASE_SETTING_TEMPLATE);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE, dec));
    }

    public LocalSetting<TestCase> newTimeout(final int index) {
        final String representation = RobotTokenType.TEST_CASE_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTimeout(index, representation);
    }

    public LocalSetting<TestCase> newTimeout(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TEST_CASE_SETTING_TIMEOUT);
        return (LocalSetting<TestCase>) addElement(index, new LocalSetting<>(ModelType.TEST_CASE_TIMEOUT, dec));
    }

    private Stream<RobotExecutableRow<TestCase>> executablesStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW)
                .map(el -> (RobotExecutableRow<TestCase>) el);
    }

    private Stream<LocalSetting<TestCase>> documentationsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_DOCUMENTATION)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    private Stream<LocalSetting<TestCase>> tagsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TAGS)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    private Stream<LocalSetting<TestCase>> setupsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_SETUP)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    private Stream<LocalSetting<TestCase>> teardownsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TEARDOWN)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    private Stream<LocalSetting<TestCase>> templatesStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TEMPLATE)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    private Stream<LocalSetting<TestCase>> timeoutsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_TIMEOUT)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    private Stream<LocalSetting<TestCase>> unknownSettingsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TEST_CASE_SETTING_UNKNOWN)
                .map(setting -> (LocalSetting<TestCase>) setting);
    }

    public List<LocalSetting<TestCase>> getDocumentation() {
        return documentationsStream().collect(toList());
    }

    public List<LocalSetting<TestCase>> getTags() {
        return tagsStream().collect(toList());
    }

    public List<LocalSetting<TestCase>> getSetups() {
        return setupsStream().collect(toList());
    }

    @Override
    public List<? extends ExecutableSetting> getSetupExecutables() {
        return setupsStream().map(setup -> setup.adaptTo(ExecutableSetting.class)).collect(toList());
    }

    public List<LocalSetting<TestCase>> getTeardowns() {
        return teardownsStream().collect(toList());
    }

    @Override
    public List<? extends ExecutableSetting> getTeardownExecutables() {
        return teardownsStream().map(teardown -> teardown.adaptTo(ExecutableSetting.class)).collect(toList());
    }

    public List<LocalSetting<TestCase>> getTemplates() {
        return templatesStream().collect(toList());
    }

    public List<LocalSetting<TestCase>> getTimeouts() {
        return timeoutsStream().collect(toList());
    }

    public List<LocalSetting<TestCase>> getUnknownSettings() {
        return unknownSettingsStream().collect(toList());
    }

    public LocalSetting<TestCase> getLastDocumentation() {
        return documentationsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<TestCase> getLastSetup() {
        return setupsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<TestCase> getLastTeardown() {
        return teardownsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<TestCase> getLastTags() {
        return tagsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<TestCase> getLastTemplate() {
        return templatesStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<TestCase> getLastTimeout() {
        return timeoutsStream().reduce((a, b) -> b).orElse(null);
    }

    @Override
    public boolean isPresent() {
        return getName() != null;
    }

    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getName().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            if (getName() != null) {
                tokens.add(getName());
            }

            for (final AModelElement<TestCase> elem : allElements) {
                tokens.addAll(elem.getElementTokens());
            }
        }
        return tokens;
    }

    public boolean isDataDrivenTestCase() {
        return getTemplateKeywordName() != null;
    }

    public String getTemplateKeywordName() {
        final String keywordName = getLocalTestTemplateInUse();

        if (keywordName == null) {
            final SettingTable settingTable = getParent().getParent().getSettingTable();
            return settingTable.isPresent() ? Strings.emptyToNull(settingTable.getTestTemplateInUse()) : null;

        } else if (keywordName.isEmpty() || keywordName.equalsIgnoreCase("none")) {
            return null;

        } else {
            return keywordName;
        }
    }

    private String getLocalTestTemplateInUse() {
        final List<LocalSetting<TestCase>> templates = getTemplates();
        if (templates.size() == 1) {
            final LocalSetting<TestCase> template = templates.get(0);
            return template.getTokensWithoutDeclaration().stream()
                    .filter(t -> t != null)
                    .map(RobotToken::getText)
                    .collect(joining(" "));
        }
        return null;
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
                    .collect(toList())
                    .indexOf(setting) > 0;
        }
    }

    @Override
    public boolean removeElementToken(final int index) {
        throw new UnsupportedOperationException("This operation is not allowed inside TestCase.");
    }

    @Override
    public FilePosition getEndPosition() {
        return findEndPosition(getParent().getParent());
    }
}
