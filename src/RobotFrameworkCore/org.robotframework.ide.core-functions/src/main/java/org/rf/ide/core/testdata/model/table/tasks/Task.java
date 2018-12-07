/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.tasks;

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
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Strings;

public class Task extends CommonCase<TaskTable, Task> implements Serializable {

    private static final long serialVersionUID = -4566881252232821271L;

    private RobotToken taskName;

    private final List<AModelElement<Task>> allElements = new ArrayList<>();

    public Task(final RobotToken testName) {
        this.taskName = fixForTheType(testName, RobotTokenType.TASK_NAME, true);
    }

    private RobotVersion getVersion() {
        return getParent().getParent().getParent().getRobotVersion();
    }

    @Override
    public Task getHolder() {
        return this;
    }

    @Override
    public ModelType getModelType() {
        return ModelType.TASK;
    }

    @Override
    public boolean isPresent() {
        return taskName != null;
    }

    @Override
    public RobotToken getName() {
        return taskName;
    }

    @Override
    public void setName(final RobotToken testName) {
        this.taskName = fixForTheType(testName, RobotTokenType.TASK_NAME, true);
    }

    @Override
    public RobotToken getDeclaration() {
        return taskName;
    }

    public AModelElement<Task> addElement(final AModelElement<?> element) {
        return addElement(allElements.size(), element);
    }

    public AModelElement<Task> addElement(final int index, final AModelElement<?> element) {
        @SuppressWarnings("unchecked")
        final AModelElement<Task> elementCasted = (AModelElement<Task>) element;
        elementCasted.setParent(this);
        allElements.add(index, elementCasted);

        if (element.getModelType() == ModelType.TASK_DOCUMENTATION) {
            final IRegionCacheable<IDocumentationHolder> adapter = ((LocalSetting<Task>) elementCasted)
                    .adaptTo(IDocumentationHolder.class);
            getParent().getParent().getParent().getDocumentationCacher().register(adapter);
        }
        return elementCasted;
    }

    @Override
    public boolean removeElement(final AModelElement<Task> element) {
        return allElements.remove(element);
    }

    public void removeElementAt(final int index) {
        allElements.remove(index);
    }

    public boolean moveElementUp(final AModelElement<Task> element) {
        return MoveElementHelper.moveUp(allElements, element);
    }

    public boolean moveElementDown(final AModelElement<Task> element) {
        return MoveElementHelper.moveDown(allElements, element);
    }

    public void replaceElement(final AModelElement<Task> oldElement, final AModelElement<Task> newElement) {
        newElement.setParent(this);
        allElements.set(allElements.indexOf(oldElement), newElement);
    }

    public void removeAllElements() {
        allElements.clear();
    }

    public List<AModelElement<Task>> getAllElements() {
        return Collections.unmodifiableList(allElements);
    }

    @Override
    public List<AModelElement<Task>> getElements() {
        return getAllElements();
    }

    @Override
    public List<RobotExecutableRow<Task>> getExecutionContext() {
        return executablesStream().collect(toList());
    }

    public LocalSetting<Task> newUnknownSetting(final int index) {
        return newUnknownSetting(index, "[]");
    }

    public LocalSetting<Task> newUnknownSetting(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_SETTING_UNKNOWN, dec));
    }

    public LocalSetting<Task> newDocumentation(final int index) {
        final String representation = RobotTokenType.TASK_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newDocumentation(index, representation);
    }

    public LocalSetting<Task> newDocumentation(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_DOCUMENTATION);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_DOCUMENTATION, dec));
    }

    public LocalSetting<Task> newTags(final int index) {
        final String representation = RobotTokenType.TASK_SETTING_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTags(index, representation);
    }

    public LocalSetting<Task> newTags(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_TAGS_DECLARATION);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_TAGS, dec));
    }

    public LocalSetting<Task> newSetup(final int index) {
        final String representation = RobotTokenType.TASK_SETTING_SETUP
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newSetup(index, representation);
    }

    public LocalSetting<Task> newSetup(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_SETUP);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_SETUP, dec));
    }

    public LocalSetting<Task> newTeardown(final int index) {
        final String representation = RobotTokenType.TASK_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTeardown(index, representation);
    }

    public LocalSetting<Task> newTeardown(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_TEARDOWN);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_TEARDOWN, dec));
    }

    public LocalSetting<Task> newTemplate(final int index) {
        final String representation = RobotTokenType.TASK_SETTING_TEMPLATE
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTemplate(index, representation);
    }

    public LocalSetting<Task> newTemplate(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_TEMPLATE);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_TEMPLATE, dec));
    }

    public LocalSetting<Task> newTimeout(final int index) {
        final String representation = RobotTokenType.TASK_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTimeout(index, representation);
    }

    public LocalSetting<Task> newTimeout(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.TASK_SETTING_TIMEOUT);
        return (LocalSetting<Task>) addElement(index, new LocalSetting<>(ModelType.TASK_TIMEOUT, dec));
    }

    private Stream<RobotExecutableRow<Task>> executablesStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_EXECUTABLE_ROW)
                .map(el -> (RobotExecutableRow<Task>) el);
    }

    private Stream<LocalSetting<Task>> documentationsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_DOCUMENTATION)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    private Stream<LocalSetting<Task>> tagsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_TAGS)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    private Stream<LocalSetting<Task>> setupsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_SETUP)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    private Stream<LocalSetting<Task>> teardownsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_TEARDOWN)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    private Stream<LocalSetting<Task>> templatesStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_TEMPLATE)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    private Stream<LocalSetting<Task>> timeoutsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_TIMEOUT)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    private Stream<LocalSetting<Task>> unknownSettingsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.TASK_SETTING_UNKNOWN)
                .map(setting -> (LocalSetting<Task>) setting);
    }

    public List<LocalSetting<Task>> getDocumentation() {
        return documentationsStream().collect(toList());
    }

    public List<LocalSetting<Task>> getTags() {
        return tagsStream().collect(toList());
    }

    public List<LocalSetting<Task>> getSetups() {
        return setupsStream().collect(toList());
    }

    @Override
    public List<? extends ExecutableSetting> getSetupExecutables() {
        return setupsStream().map(setup -> setup.adaptTo(ExecutableSetting.class)).collect(toList());
    }

    public List<LocalSetting<Task>> getTeardowns() {
        return teardownsStream().collect(toList());
    }

    @Override
    public List<? extends ExecutableSetting> getTeardownExecutables() {
        return teardownsStream().map(teardown -> teardown.adaptTo(ExecutableSetting.class)).collect(toList());
    }

    public List<LocalSetting<Task>> getTemplates() {
        return templatesStream().collect(toList());
    }

    public List<LocalSetting<Task>> getTimeouts() {
        return timeoutsStream().collect(toList());
    }

    public List<LocalSetting<Task>> getUnknownSettings() {
        return unknownSettingsStream().collect(toList());
    }

    public LocalSetting<Task> getLastDocumentation() {
        return documentationsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<Task> getLastSetup() {
        return setupsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<Task> getLastTeardown() {
        return teardownsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<Task> getLastTags() {
        return tagsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<Task> getLastTemplate() {
        return templatesStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<Task> getLastTimeout() {
        return timeoutsStream().reduce((a, b) -> b).orElse(null);
    }

    @Override
    public FilePosition getBeginPosition() {
        return getName().getFilePosition();
    }

    @Override
    public FilePosition getEndPosition() {
        return findEndPosition(getParent().getParent());
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(taskName);

            for (final AModelElement<Task> elem : allElements) {
                tokens.addAll(elem.getElementTokens());
            }
        }
        return tokens;
    }

    public boolean isDataDrivenTask() {
        return getTemplateKeywordName() != null;
    }

    public String getTemplateKeywordName() {
        final String keywordName = getLocalTaskTemplateInUse();

        if (keywordName == null) {
            final SettingTable settingTable = getParent().getParent().getSettingTable();
            return settingTable.isPresent() ? Strings.emptyToNull(settingTable.getTaskTemplateInUse()) : null;

        } else if (keywordName.isEmpty() || keywordName.equalsIgnoreCase("none")) {
            return null;

        } else {
            return keywordName;
        }
    }

    private String getLocalTaskTemplateInUse() {
        final List<LocalSetting<Task>> templates = getTemplates();
        if (templates.size() == 1) {
            final LocalSetting<Task> template = templates.get(0);
            return template.getTokensWithoutDeclaration().stream()
                    .filter(t -> t != null)
                    .map(RobotToken::getText)
                    .collect(joining(" "));
        }
        return null;
    }

    public boolean isDuplicatedSetting(final AModelElement<Task> setting) {
        if (setting.getModelType() == ModelType.TASK_SETTING_UNKNOWN) {
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
        throw new UnsupportedOperationException("This operation is not allowed inside Task.");
    }
}
