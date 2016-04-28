/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TableHeader<T> extends AModelElement<T> {

    private final RobotToken tableHeader;

    private final List<RobotToken> columnNames = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public TableHeader(final RobotToken tableHeader) {
        this.tableHeader = tableHeader;
    }

    public void addColumnName(final RobotToken columnName) {
        columnNames.add(columnName);
    }

    public List<RobotToken> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public RobotToken getTableHeader() {
        return tableHeader;
    }

    @Override
    public RobotToken getDeclaration() {
        return tableHeader;
    }

    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    public void addComment(final RobotToken commentWord) {
        fixComment(comment, commentWord);
        comment.add(commentWord);
    }

    @Override
    public boolean isPresent() {
        return (tableHeader != null);
    }

    @Override
    public ModelType getModelType() {
        ModelType type = ModelType.UNKNOWN;
        if (isPresent()) {
            final IRobotTokenType tokenType = tableHeader.getTypes().get(0);
            if (tokenType == RobotTokenType.SETTINGS_TABLE_HEADER) {
                type = ModelType.SETTINGS_TABLE_HEADER;
            } else if (tokenType == RobotTokenType.VARIABLES_TABLE_HEADER) {
                type = ModelType.VARIABLES_TABLE_HEADER;
            } else if (tokenType == RobotTokenType.TEST_CASES_TABLE_HEADER) {
                type = ModelType.TEST_CASE_TABLE_HEADER;
            } else if (tokenType == RobotTokenType.KEYWORDS_TABLE_HEADER) {
                type = ModelType.KEYWORDS_TABLE_HEADER;
            }
        }

        return type;
    }

    @Override
    public FilePosition getBeginPosition() {
        FilePosition pos = FilePosition.createNotSet();
        if (isPresent()) {
            pos = tableHeader.getFilePosition();
        }
        return pos;
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getTableHeader());
            tokens.addAll(getColumnNames());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public String toString() {
        return String.format("TableHeader [tableHeader=%s, columnNames=%s, comment=%s]", tableHeader, columnNames,
                comment);
    }
}
