/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TableHeaderCommentMapper implements IHashCommentMapper {

    private final ParsingStateHelper stateHelper;


    public TableHeaderCommentMapper() {
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public boolean isApplicable(ParsingState state) {
        return stateHelper.isTableState(state);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        ARobotSectionTable table = null;
        if (currentState == ParsingState.SETTING_TABLE_HEADER) {
            table = fileModel.getSettingTable();
        } else if (currentState == ParsingState.VARIABLE_TABLE_HEADER) {
            table = fileModel.getVariableTable();
        } else if (currentState == ParsingState.KEYWORD_TABLE_HEADER) {
            table = fileModel.getKeywordTable();
        } else if (currentState == ParsingState.TEST_CASE_TABLE_HEADER) {
            table = fileModel.getTestCaseTable();
        }

        List<TableHeader<? extends ARobotSectionTable>> headers = table
                .getHeaders();
        if (!headers.isEmpty()) {
            @SuppressWarnings("rawtypes")
            TableHeader header = headers.get(headers.size() - 1);
            header.addComment(rt);
        } else {
            // FIXME: it is not possible
        }
    }
}
