/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
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

    public RobotToken getKeywordName() {
        return keywordName;
    }

    public void setKeywordName(final RobotToken keywordName) {
        fixForTheType(keywordName, RobotTokenType.KEYWORD_NAME, true);
        this.keywordName = keywordName;
    }

    public void addElement(final AModelElement<UserKeyword> element) {
        element.setParent(this);
        allElements.add(element);
    }

    public void addElement(final AModelElement<UserKeyword> element, final int index) {
        element.setParent(this);
        allElements.add(index, element);
    }

    @Override
    public boolean removeElement(final AModelElement<UserKeyword> element) {
        if (element != null) {
            return allElements.remove(element);
        }
        return false;
    }

    public void removeElementAt(final int index) {
        allElements.remove(index);
    }

    public boolean moveElementUp(final AModelElement<UserKeyword> element) {
        return MoveElementHelper.moveUp(allElements, element);
    }

    public boolean moveElementDown(final AModelElement<UserKeyword> element) {
        return MoveElementHelper.moveDown(allElements, element);
    }

    public void replaceElement(final AModelElement<UserKeyword> oldElement,
            final AModelElement<UserKeyword> newElement) {
        newElement.setParent(this);
        allElements.set(allElements.indexOf(oldElement), newElement);
    }

    public void removeAllElements() {
        allElements.clear();
    }

    public List<AModelElement<UserKeyword>> getAllElements() {
        return Collections.unmodifiableList(allElements);
    }

    @Override
    public List<AModelElement<UserKeyword>> getElements() {
        return getAllElements();
    }

    @Override
    public List<RobotExecutableRow<UserKeyword>> getExecutionContext() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW)
                .map(el -> (RobotExecutableRow<UserKeyword>) el)
                .collect(Collectors.toList());
    }

    public KeywordDocumentation newDocumentation(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);

        final KeywordDocumentation keyDoc = new KeywordDocumentation(dec);
        addDocumentation(index, keyDoc);

        return keyDoc;
    }

    public void addDocumentation(final KeywordDocumentation doc) {
        addDocumentation(allElements.size(), doc);
    }

    public void addDocumentation(final int index, final KeywordDocumentation doc) {
        doc.setParent(this);
        allElements.add(index, doc);
        getParent().getParent().getParent().getDocumentationCacher().register(doc);
    }

    public KeywordTags newTags(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TAGS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TAGS);

        final KeywordTags keyTags = new KeywordTags(dec);
        addElement(keyTags, index);

        return keyTags;
    }

    public KeywordArguments newArguments(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_ARGUMENTS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_ARGUMENTS);

        final KeywordArguments keyArgs = new KeywordArguments(dec);
        addElement(keyArgs, index);

        return keyArgs;
    }

    public KeywordReturn newReturn(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_RETURN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_RETURN);

        final KeywordReturn keyReturn = new KeywordReturn(dec);
        addElement(keyReturn, index);

        return keyReturn;
    }

    public KeywordTeardown newTeardown(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TEARDOWN);

        final KeywordTeardown keyTeardown = new KeywordTeardown(dec);
        addElement(keyTeardown, index);

        return keyTeardown;
    }

    public KeywordTimeout newTimeout(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TIMEOUT);

        final KeywordTimeout keyTimeout = new KeywordTimeout(dec);
        addElement(keyTimeout, index);

        return keyTimeout;
    }

    public KeywordUnknownSettings newUnknownSettings(final int index) {
        final RobotToken dec = RobotToken.create("[]",
                newArrayList(RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION));

        final KeywordUnknownSettings unknown = new KeywordUnknownSettings(dec);
        addElement(unknown, index);

        return unknown;
    }

    public List<KeywordDocumentation> getDocumentation() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION)
                .map(KeywordDocumentation.class::cast)
                .collect(Collectors.toList());
    }

    public List<KeywordTags> getTags() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_TAGS)
                .map(KeywordTags.class::cast)
                .collect(Collectors.toList());
    }

    public List<KeywordArguments> getArguments() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_ARGUMENTS)
                .map(KeywordArguments.class::cast)
                .collect(Collectors.toList());
    }

    public List<KeywordReturn> getReturns() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_RETURN)
                .map(KeywordReturn.class::cast)
                .collect(Collectors.toList());
    }

    public List<KeywordTeardown> getTeardowns() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_TEARDOWN)
                .map(KeywordTeardown.class::cast)
                .collect(Collectors.toList());
    }

    public List<KeywordTimeout> getTimeouts() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_TIMEOUT)
                .map(KeywordTimeout.class::cast)
                .collect(Collectors.toList());
    }

    public List<KeywordUnknownSettings> getUnknownSettings() {
        return getElements().stream()
                .filter(el -> el.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN)
                .map(KeywordUnknownSettings.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPresent() {
        return (getKeywordName() != null);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getKeywordName().getFilePosition();
    }

    @Override
    public RobotToken getDeclaration() {
        return getKeywordName();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            if (getKeywordName() != null) {
                tokens.add(getKeywordName());
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
                    .collect(Collectors.toList())
                    .indexOf(setting) > 0;
        }
    }

    @Override
    public RobotToken getName() {
        return getKeywordName();
    }

    @Override
    public FilePosition getEndPosition() {
        return findEndPosition(getParent().getParent());
    }
}
