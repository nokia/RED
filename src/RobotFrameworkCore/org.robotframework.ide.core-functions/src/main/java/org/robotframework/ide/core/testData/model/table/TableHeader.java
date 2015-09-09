/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TableHeader extends AModelElement {

    private final RobotToken tableHeader;
    private List<RobotToken> columnNames = new LinkedList<>();
    private List<RobotToken> comment = new LinkedList<>();


    public TableHeader(final RobotToken tableHeader) {
        this.tableHeader = tableHeader;
    }


    public void addColumnName(RobotToken columnName) {
        columnNames.add(columnName);
    }


    public List<RobotToken> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }


    public RobotToken getTableHeader() {
        return tableHeader;
    }


    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }


    public void addComment(RobotToken commentWord) {
        this.comment.add(commentWord);
    }


    @Override
    public boolean isPresent() {
        return (tableHeader != null);
    }


    @Override
    public ModelType getModelType() {
        ModelType type = ModelType.UNKNOWN;
        if (isPresent()) {
            IRobotTokenType tokenType = tableHeader.getTypes().get(0);
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
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getTableHeader());
            tokens.addAll(getColumnNames());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
