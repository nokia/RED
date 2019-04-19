/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

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
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UserKeyword extends AModelElement<KeywordTable>
        implements IExecutableStepsHolder<UserKeyword>, Serializable {

    private static final long serialVersionUID = -7553229094807229714L;

    private RobotToken keywordName;

    private final List<AModelElement<UserKeyword>> allElements = new ArrayList<>();

    public UserKeyword(final RobotToken keywordName) {
        this.keywordName = keywordName;
        fixForTheType(keywordName, RobotTokenType.KEYWORD_NAME);
    }

    private RobotVersion getVersion() {
        return getParent().getParent().getParent().getRobotVersion();
    }

    @Override
    public RobotToken getName() {
        return keywordName;
    }

    @Override
    public void setName(final RobotToken keywordName) {
        fixForTheType(keywordName, RobotTokenType.KEYWORD_NAME, true);
        this.keywordName = keywordName;
    }

    @Override
    public AModelElement<UserKeyword> addElement(final AModelElement<?> element) {
        return addElement(allElements.size(), element);
    }

    @Override
    public AModelElement<UserKeyword> addElement(final int index, final AModelElement<?> element) {
        @SuppressWarnings("unchecked")
        final AModelElement<UserKeyword> elementCasted = (AModelElement<UserKeyword>) element;
        elementCasted.setParent(this);
        allElements.add(index, elementCasted);

        if (element.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION) {
            final IRegionCacheable<IDocumentationHolder> adapter = ((LocalSetting<?>) elementCasted)
                    .adaptTo(IDocumentationHolder.class);
            getParent().getParent().getParent().getDocumentationCacher().register(adapter);
        }
        return elementCasted;
    }

    @Override
    public boolean removeElement(final AModelElement<UserKeyword> element) {
        return allElements.remove(element);
    }

    @Override
    public void removeElement(final int index) {
        allElements.remove(index);
    }

    public boolean moveElementUp(final AModelElement<UserKeyword> element) {
        return MoveElementHelper.moveUp(allElements, element);
    }

    public boolean moveElementDown(final AModelElement<UserKeyword> element) {
        return MoveElementHelper.moveDown(allElements, element);
    }

    @Override
    public void replaceElement(final AModelElement<UserKeyword> oldElement,
            final AModelElement<UserKeyword> newElement) {
        newElement.setParent(this);
        allElements.set(allElements.indexOf(oldElement), newElement);
    }

    public void removeAllElements() {
        allElements.clear();
    }

    @Override
    public List<AModelElement<UserKeyword>> getElements() {
        return Collections.unmodifiableList(allElements);
    }

    @Override
    public List<RobotExecutableRow<UserKeyword>> getExecutionContext() {
        return executablesStream().collect(toList());
    }

    public LocalSetting<UserKeyword> newDocumentation(final int index) {
        final String representation = RobotTokenType.KEYWORD_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newDocumentation(index, representation);
    }

    public LocalSetting<UserKeyword> newDocumentation(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);
        return (LocalSetting<UserKeyword>) addElement(index,
                new LocalSetting<>(ModelType.USER_KEYWORD_DOCUMENTATION, dec));
    }

    public LocalSetting<UserKeyword> newTags(final int index) {
        final String representation = RobotTokenType.KEYWORD_SETTING_TAGS
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTags(index, representation);
    }

    public LocalSetting<UserKeyword> newTags(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_TAGS);
        return (LocalSetting<UserKeyword>) addElement(index, new LocalSetting<>(ModelType.USER_KEYWORD_TAGS, dec));
    }

    public LocalSetting<UserKeyword> newArguments(final int index) {
        final String representation = RobotTokenType.KEYWORD_SETTING_ARGUMENTS
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newArguments(index, representation);
    }

    public LocalSetting<UserKeyword> newArguments(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_ARGUMENTS);
        return (LocalSetting<UserKeyword>) addElement(index, new LocalSetting<>(ModelType.USER_KEYWORD_ARGUMENTS, dec));
    }

    public LocalSetting<UserKeyword> newReturn(final int index) {
        final String representation = RobotTokenType.KEYWORD_SETTING_RETURN
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newReturn(index, representation);
    }

    public LocalSetting<UserKeyword> newReturn(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_RETURN);
        return (LocalSetting<UserKeyword>) addElement(index,
                new LocalSetting<UserKeyword>(ModelType.USER_KEYWORD_RETURN, dec));
    }

    public LocalSetting<UserKeyword> newTeardown(final int index) {
        final String representation = RobotTokenType.KEYWORD_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTeardown(index, representation);
    }

    public LocalSetting<UserKeyword> newTeardown(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_TEARDOWN);
        return (LocalSetting<UserKeyword>) addElement(index, new LocalSetting<>(ModelType.USER_KEYWORD_TEARDOWN, dec));
    }

    public LocalSetting<UserKeyword> newTimeout(final int index) {
        final String representation = RobotTokenType.KEYWORD_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getVersion())
                .getRepresentation();
        return newTimeout(index, representation);
    }

    public LocalSetting<UserKeyword> newTimeout(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_TIMEOUT);
        return (LocalSetting<UserKeyword>) addElement(index, new LocalSetting<>(ModelType.USER_KEYWORD_TIMEOUT, dec));
    }

    public LocalSetting<UserKeyword> newUnknownSettings(final int index) {
        return newUnknownSetting(index, "[]");
    }

    public LocalSetting<UserKeyword> newUnknownSetting(final int index, final String representation) {
        final RobotToken dec = RobotToken.create(representation, RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION);
        return (LocalSetting<UserKeyword>) addElement(index, new LocalSetting<>(ModelType.USER_KEYWORD_SETTING_UNKNOWN, dec));
    }

    public List<LocalSetting<UserKeyword>> getDocumentation() {
        return documentationsStream().collect(toList());
    }

    public List<LocalSetting<UserKeyword>> getTags() {
        return tagsStream().collect(toList());
    }

    public List<LocalSetting<UserKeyword>> getArguments() {
        return argumentsStream().collect(toList());
    }

    public List<LocalSetting<UserKeyword>> getReturns() {
        return returnsStream().collect(toList());
    }

    public List<LocalSetting<UserKeyword>> getTeardowns() {
        return teardownsStream().collect(toList());
    }

    public List<? extends ExecutableSetting> getTeardownExecutables() {
        return teardownsStream().map(teardown -> teardown.adaptTo(ExecutableSetting.class)).collect(toList());
    }

    public List<LocalSetting<UserKeyword>> getTimeouts() {
        return timeoutsStream().collect(toList());
    }

    public List<LocalSetting<UserKeyword>> getUnknownSettings() {
        return unknownSettingsStream().collect(toList());
    }

    private Stream<RobotExecutableRow<UserKeyword>> executablesStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW)
                .map(el -> (RobotExecutableRow<UserKeyword>) el);
    }

    private Stream<LocalSetting<UserKeyword>> documentationsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    private Stream<LocalSetting<UserKeyword>> tagsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_TAGS)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    private Stream<LocalSetting<UserKeyword>> argumentsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_ARGUMENTS)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    private Stream<LocalSetting<UserKeyword>> returnsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_RETURN)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    private Stream<LocalSetting<UserKeyword>> teardownsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_TEARDOWN)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    private Stream<LocalSetting<UserKeyword>> timeoutsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_TIMEOUT)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    private Stream<LocalSetting<UserKeyword>> unknownSettingsStream() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN)
                .map(setting -> (LocalSetting<UserKeyword>) setting);
    }

    public LocalSetting<UserKeyword> getLastDocumentation() {
        return documentationsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<UserKeyword> getLastTags() {
        return tagsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<UserKeyword> getLastArgument() {
        return argumentsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<UserKeyword> getLastReturn() {
        return returnsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<UserKeyword> getLastTeardown() {
        return teardownsStream().reduce((a, b) -> b).orElse(null);
    }

    public LocalSetting<UserKeyword> getLastTimeout() {
        return timeoutsStream().reduce((a, b) -> b).orElse(null);
    }

    @Override
    public boolean isPresent() {
        return getName() != null;
    }

    @Override
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getName().getFilePosition();
    }

    @Override
    public RobotToken getDeclaration() {
        return getName();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            if (getName() != null) {
                tokens.add(getName());
            }

            for (final AModelElement<UserKeyword> elem : allElements) {
                tokens.addAll(elem.getElementTokens());
            }
        }
        return tokens;
    }

    @Override
    public UserKeyword getHolder() {
        return this;
    }

    @Override
    public boolean removeElementToken(final int index) {
        throw new UnsupportedOperationException("This operation is not allowed inside UserKeyword.");
    }

    public boolean isDuplicatedSetting(final AModelElement<UserKeyword> setting) {
        if (setting.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN) {
            return false;
        } else {
            return allElements.stream()
                    .filter(el -> el.getModelType() == setting.getModelType())
                    .collect(toList())
                    .indexOf(setting) > 0;
        }
    }

    @Override
    public FilePosition getEndPosition() {
        return findEndPosition(getParent().getParent());
    }
}
