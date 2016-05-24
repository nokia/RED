/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords;

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

public class UserKeyword extends AModelElement<KeywordTable> implements IExecutableStepsHolder<UserKeyword> {

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
    }

    public RobotToken getKeywordName() {
        return keywordName;
    }

    public void setKeywordName(final RobotToken keywordName) {
        fixForTheType(keywordName, RobotTokenType.KEYWORD_NAME, true);
        this.keywordName = keywordName;
    }

    public void addUnknownSettings(final KeywordUnknownSettings unknownSetting) {
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
        this.keywordContext.set(position, executionRow);
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
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);

        KeywordDocumentation keyDoc = new KeywordDocumentation(dec);
        addDocumentation(keyDoc);

        return keyDoc;
    }

    public void addDocumentation(final KeywordDocumentation doc) {
        doc.setParent(this);
        this.documentation.add(doc);
    }

    public List<KeywordDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentation);
    }

    public KeywordTags newTags() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TAGS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TAGS);

        KeywordTags keyTags = new KeywordTags(dec);
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
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_ARGUMENTS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_ARGUMENTS);

        KeywordArguments keyArgs = new KeywordArguments(dec);
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
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_RETURN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_RETURN);

        KeywordReturn keyReturn = new KeywordReturn(dec);
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
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TEARDOWN
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TEARDOWN);

        KeywordTeardown keyTeardown = new KeywordTeardown(dec);
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
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.KEYWORD_SETTING_TIMEOUT
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        fixForTheType(dec, RobotTokenType.KEYWORD_SETTING_TIMEOUT);

        KeywordTimeout keyTimeout = new KeywordTimeout(dec);
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

            for (final RobotExecutableRow<UserKeyword> row : keywordContext) {
                tokens.addAll(row.getElementTokens());
            }

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

            Collections.sort(tokens, new RobotTokenPositionComparator());
        }

        return tokens;
    }

    @Override
    public UserKeyword getHolder() {
        return this;
    }

    @Override
    public List<AModelElement<UserKeyword>> getUnitSettings() {
        List<AModelElement<UserKeyword>> settings = new ArrayList<>();
        settings.addAll(getDocumentation());
        settings.addAll(getTags());
        settings.addAll(getArguments());
        settings.addAll(getReturns());
        settings.addAll(getTeardowns());
        settings.addAll(getTimeouts());
        settings.addAll(getUnknownSettings());

        return settings;
    }
}
