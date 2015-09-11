/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.RobotTokenPositionComparator;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UserKeyword extends AModelElement<KeywordTable> {

    private RobotToken keywordName;
    private final List<KeywordDocumentation> documentation = new LinkedList<>();
    private final List<KeywordTags> tags = new LinkedList<>();
    private final List<KeywordArguments> keywordArguments = new LinkedList<>();
    private final List<KeywordReturn> keywordReturns = new LinkedList<>();
    private final List<KeywordTeardown> teardowns = new LinkedList<>();
    private final List<KeywordTimeout> timeouts = new LinkedList<>();
    private final List<RobotExecutableRow<UserKeyword>> keywordContext = new LinkedList<>();


    public UserKeyword(final RobotToken keywordName) {
        this.keywordName = keywordName;
    }


    public RobotToken getKeywordName() {
        return keywordName;
    }


    public void setKeywordName(final RobotToken keywordName) {
        this.keywordName = keywordName;
    }


    public void addKeywordExecutionRow(
            final RobotExecutableRow<UserKeyword> executionRow) {
        executionRow.setParent(this);
        this.keywordContext.add(executionRow);
    }


    public List<RobotExecutableRow<UserKeyword>> getKeywordExecutionRows() {
        return Collections.unmodifiableList(keywordContext);
    }


    public void addDocumentation(final KeywordDocumentation doc) {
        doc.setParent(this);
        this.documentation.add(doc);
    }


    public List<KeywordDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentation);
    }


    public void addTag(final KeywordTags tag) {
        tag.setParent(this);
        tags.add(tag);
    }


    public List<KeywordTags> getTags() {
        return Collections.unmodifiableList(tags);
    }


    public void addArguments(final KeywordArguments arguments) {
        arguments.setParent(this);
        keywordArguments.add(arguments);
    }


    public List<KeywordArguments> getArguments() {
        return Collections.unmodifiableList(keywordArguments);
    }


    public void addReturn(final KeywordReturn keywordReturn) {
        keywordReturn.setParent(this);
        keywordReturns.add(keywordReturn);
    }


    public List<KeywordReturn> getReturns() {
        return Collections.unmodifiableList(keywordReturns);
    }


    public void addTeardown(final KeywordTeardown teardown) {
        teardown.setParent(this);
        teardowns.add(teardown);
    }


    public List<KeywordTeardown> getTeardowns() {
        return Collections.unmodifiableList(teardowns);
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
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            if (getKeywordName() != null) {
                tokens.add(getKeywordName());
            }

            for (KeywordDocumentation doc : documentation) {
                tokens.addAll(doc.getElementTokens());
            }

            for (KeywordArguments arguments : keywordArguments) {
                tokens.addAll(arguments.getElementTokens());
            }

            for (RobotExecutableRow<UserKeyword> row : keywordContext) {
                tokens.addAll(row.getElementTokens());
            }

            for (KeywordReturn returns : keywordReturns) {
                tokens.addAll(returns.getElementTokens());
            }

            for (KeywordTags tag : tags) {
                tokens.addAll(tag.getElementTokens());
            }

            for (KeywordTeardown teardown : teardowns) {
                tokens.addAll(teardown.getElementTokens());
            }

            for (KeywordTimeout timeout : timeouts) {
                tokens.addAll(timeout.getElementTokens());
            }

            Collections.sort(tokens, new RobotTokenPositionComparator());
        }

        return tokens;
    }
}
