/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.RobotTokenPositionComparator;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class UserKeyword extends AModelElement<KeywordTable>
        implements IExecutableStepsHolder<UserKeyword>, Serializable {

    private static final long serialVersionUID = -7553229094807229714L;

    private RobotToken keywordName;

    private final List<KeywordDocumentation> documentation = new ArrayList<>();

    private final List<KeywordTags> tags = new ArrayList<>();

    private final List<KeywordArguments> keywordArguments = new ArrayList<>();

    private final List<KeywordReturn> keywordReturns = new ArrayList<>();

    private final List<KeywordTeardown> teardowns = new ArrayList<>();

    private final List<KeywordTimeout> timeouts = new ArrayList<>();

    private final List<KeywordUnknownSettings> unknownSettings = new ArrayList<>(0);

    private final List<RobotExecutableRow<UserKeyword>> keywordContext = new ArrayList<>();

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

    public void addUnknownSettings(final KeywordUnknownSettings unknownSetting) {
        unknownSetting.setParent(this);
        this.unknownSettings.add(unknownSetting);
    }

    public List<KeywordUnknownSettings> getUnknownSettings() {
        return Collections.unmodifiableList(unknownSettings);
    }

    public void addKeywordExecutionRow(final RobotExecutableRow<UserKeyword> executionRow) {
        executionRow.setParent(this);
        this.keywordContext.add(executionRow);
    }

    public void addKeywordExecutionRow(final RobotExecutableRow<UserKeyword> executionRow, final int position) {
        executionRow.setParent(this);
        this.keywordContext.add(position, executionRow);
    }

    public void removeExecutableRow(final RobotExecutableRow<UserKeyword> executionRow) {
        this.keywordContext.remove(executionRow);
    }

    public boolean moveUpExecutableRow(final RobotExecutableRow<UserKeyword> executionRow) {
        return MoveElementHelper.moveUp(keywordContext, executionRow);
    }

    public boolean moveDownExecutableRow(final RobotExecutableRow<UserKeyword> executionRow) {
        return MoveElementHelper.moveDown(keywordContext, executionRow);
    }

    public void removeExecutableLineWithIndex(final int rowIndex) {
        this.keywordContext.remove(rowIndex);
    }

    public void removeAllKeywordExecutionRows() {
        keywordContext.clear();
    }

    public List<RobotExecutableRow<UserKeyword>> getKeywordExecutionRows() {
        return Collections.unmodifiableList(keywordContext);
    }

    @Override
    public List<RobotExecutableRow<UserKeyword>> getExecutionContext() {
        return getKeywordExecutionRows();
    }

    public KeywordDocumentation newDocumentation() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);

        final KeywordDocumentation keyDoc = new KeywordDocumentation(dec);
        addDocumentation(keyDoc);

        return keyDoc;
    }

    public void addDocumentation(final KeywordDocumentation doc) {
        doc.setParent(this);
        this.documentation.add(doc);
        getParent().getParent().getParent().getDocumentationCacher().register(doc);
    }

    public List<KeywordDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentation);
    }

    public KeywordTags newTags() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TAGS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TAGS);

        final KeywordTags keyTags = new KeywordTags(dec);
        addTag(keyTags);

        return keyTags;
    }

    public void addTag(final KeywordTags tag) {
        tag.setParent(this);
        tags.add(tag);
    }

    public List<KeywordTags> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public KeywordArguments newArguments() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_ARGUMENTS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_ARGUMENTS);

        final KeywordArguments keyArgs = new KeywordArguments(dec);
        addArguments(keyArgs);

        return keyArgs;
    }

    public void addArguments(final KeywordArguments arguments) {
        arguments.setParent(this);
        keywordArguments.add(arguments);
    }

    public List<KeywordArguments> getArguments() {
        return Collections.unmodifiableList(keywordArguments);
    }

    public KeywordReturn newReturn() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_RETURN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_RETURN);

        final KeywordReturn keyReturn = new KeywordReturn(dec);
        addReturn(keyReturn);

        return keyReturn;
    }

    public void addReturn(final KeywordReturn keywordReturn) {
        keywordReturn.setParent(this);
        keywordReturns.add(keywordReturn);
    }

    public List<KeywordReturn> getReturns() {
        return Collections.unmodifiableList(keywordReturns);
    }

    public KeywordTeardown newTeardown() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TEARDOWN);

        final KeywordTeardown keyTeardown = new KeywordTeardown(dec);
        addTeardown(keyTeardown);

        return keyTeardown;
    }

    public void addTeardown(final KeywordTeardown teardown) {
        teardown.setParent(this);
        teardowns.add(teardown);
    }

    public List<KeywordTeardown> getTeardowns() {
        return Collections.unmodifiableList(teardowns);
    }

    public KeywordTimeout newTimeout() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TIMEOUT);

        final KeywordTimeout keyTimeout = new KeywordTimeout(dec);
        addTimeout(keyTimeout);

        return keyTimeout;
    }

    public void addTimeout(final KeywordTimeout timeout) {
        timeout.setParent(this);
        timeouts.add(timeout);
    }

    public List<KeywordTimeout> getTimeouts() {
        return Collections.unmodifiableList(timeouts);
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

            for (final KeywordDocumentation doc : documentation) {
                tokens.addAll(doc.getElementTokens());
            }

            for (final KeywordArguments arguments : keywordArguments) {
                tokens.addAll(arguments.getElementTokens());
            }

            final List<RobotToken> keywordContextInModel = new ArrayList<>(0);
            for (final RobotExecutableRow<UserKeyword> row : keywordContext) {
                keywordContextInModel.addAll(row.getElementTokens());
            }
            tokens.addAll(keywordContextInModel);

            for (final KeywordReturn returns : keywordReturns) {
                tokens.addAll(returns.getElementTokens());
            }

            for (final KeywordTags tag : tags) {
                tokens.addAll(tag.getElementTokens());
            }

            for (final KeywordTeardown teardown : teardowns) {
                tokens.addAll(teardown.getElementTokens());
            }

            for (final KeywordTimeout timeout : timeouts) {
                tokens.addAll(timeout.getElementTokens());
            }

            for (final KeywordUnknownSettings setting : unknownSettings) {
                tokens.addAll(setting.getElementTokens());
            }

            Collections.sort(tokens, new RobotTokenPositionComparator());
            positionRevertToExpectedOrder(tokens, keywordContextInModel);
        }

        return tokens;
    }

    @Override
    public UserKeyword getHolder() {
        return this;
    }

    @Override
    public List<AModelElement<UserKeyword>> getUnitSettings() {
        final List<AModelElement<UserKeyword>> settings = new ArrayList<>();
        settings.addAll(getDocumentation());
        settings.addAll(getTags());
        settings.addAll(getArguments());
        settings.addAll(getReturns());
        settings.addAll(getTeardowns());
        settings.addAll(getTimeouts());
        settings.addAll(getUnknownSettings());

        return settings;
    }

    @Override
    public boolean removeUnitSettings(final AModelElement<UserKeyword> setting) {
        if (setting != null) {
            final ModelType settingType = setting.getModelType();
            switch (settingType) {
                case USER_KEYWORD_DOCUMENTATION:
                    return this.documentation.remove(setting);
                case USER_KEYWORD_TAGS:
                    return this.tags.remove(setting);
                case USER_KEYWORD_ARGUMENTS:
                    return this.keywordArguments.remove(setting);
                case USER_KEYWORD_RETURN:
                    return this.keywordReturns.remove(setting);
                case USER_KEYWORD_TEARDOWN:
                    return this.teardowns.remove(setting);
                case USER_KEYWORD_TIMEOUT:
                    return this.timeouts.remove(setting);
                case USER_KEYWORD_SETTING_UNKNOWN:
                    return this.unknownSettings.remove(setting);
                default:
                    return false;
            }
        }

        return false;
    }

    @Override
    public boolean removeElementToken(final int index) {
        throw new UnsupportedOperationException("This operation is not allowed inside UserKeyword.");
    }

    public boolean isDuplicatedSetting(final AModelElement<UserKeyword> setting) {
        if (setting.getModelType() == ModelType.USER_KEYWORD_SETTING_UNKNOWN) {
            return false;
        } else {
            return getContainingList(setting).indexOf(setting) > 0;
        }
    }

    public List<? extends AModelElement<UserKeyword>> getContainingList(final AModelElement<?> setting) {
        if (setting != null) {
            final ModelType settingType = setting.getModelType();
            switch (settingType) {
                case USER_KEYWORD_DOCUMENTATION:
                    return documentation;
                case USER_KEYWORD_TAGS:
                    return tags;
                case USER_KEYWORD_TEARDOWN:
                    return teardowns;
                case USER_KEYWORD_TIMEOUT:
                    return timeouts;
                case USER_KEYWORD_ARGUMENTS:
                    return keywordArguments;
                case USER_KEYWORD_RETURN:
                    return keywordReturns;
                case USER_KEYWORD_SETTING_UNKNOWN:
                    return unknownSettings;
                default:
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
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
