/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class KeywordFinder {

    public UserKeyword findOrCreateNearestKeyword(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp) {
        RobotFile fileModel = robotFileOutput.getFileModel();
        KeywordTable keywordTable = fileModel.getKeywordTable();

        UserKeyword keyword;
        List<UserKeyword> lastHeaderKeyword = filterByKeywordAfterLastHeader(keywordTable);
        if (lastHeaderKeyword.isEmpty()) {
            keyword = createArtificialKeyword(robotFileOutput, keywordTable);
            keywordTable.addKeyword(keyword);

            RobotLine lineToModification = findRobotLineInModel(fileModel,
                    keyword, currentLine);
            lineToModification.addLineElementAt(0, keyword.getKeywordName());
        } else {
            keyword = lastHeaderKeyword.get(lastHeaderKeyword.size() - 1);
        }

        return keyword;
    }


    private RobotLine findRobotLineInModel(RobotFile fileModel,
            UserKeyword userKeyword, RobotLine currentLine) {
        RobotLine foundLine = currentLine;
        if (foundLine.getLineNumber() != userKeyword.getBeginPosition()
                .getLine()) {
            List<RobotLine> fileContent = fileModel.getFileContent();
            for (RobotLine line : fileContent) {
                if (userKeyword.getBeginPosition().getLine() == line
                        .getLineNumber()) {
                    foundLine = line;
                    break;
                }
            }
        }

        return foundLine;
    }


    private UserKeyword createArtificialKeyword(
            RobotFileOutput robotFileOutput, KeywordTable keywordTable) {
        UserKeyword keyword;
        @SuppressWarnings("rawtypes")
        List<TableHeader> headers = keywordTable.getHeaders();
        @SuppressWarnings("rawtypes")
        TableHeader tableHeader = headers.get(headers.size() - 1);
        RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader()
                .getLineNumber() + 1);
        artificialNameToken.setRaw(new StringBuilder());
        artificialNameToken.setText(new StringBuilder());
        artificialNameToken.setStartColumn(0);
        RobotLine robotLine = robotFileOutput.getFileModel().getFileContent()
                .get(tableHeader.getTableHeader().getLineNumber() - 1);
        IRobotLineElement endOfLine = robotLine.getEndOfLine();
        artificialNameToken.setStartOffset(endOfLine.getStartOffset()
                + endOfLine.getRaw().length());
        artificialNameToken.setType(RobotTokenType.KEYWORD_NAME);

        keyword = new UserKeyword(artificialNameToken);

        return keyword;
    }


    @VisibleForTesting
    protected List<UserKeyword> filterByKeywordAfterLastHeader(
            final KeywordTable keywordTable) {
        List<UserKeyword> keywords = new LinkedList<>();

        @SuppressWarnings("rawtypes")
        List<TableHeader> headers = keywordTable.getHeaders();
        if (!headers.isEmpty()) {
            List<UserKeyword> keywordsAvail = keywordTable.getKeywords();
            @SuppressWarnings("rawtypes")
            TableHeader tableHeader = headers.get(headers.size() - 1);
            int tableHeaderLineNumber = tableHeader.getTableHeader()
                    .getLineNumber();
            int numberOfTestCases = keywordsAvail.size();
            for (int i = numberOfTestCases - 1; i >= 0; i--) {
                UserKeyword keyword = keywordsAvail.get(i);
                if (keyword.getKeywordName().getLineNumber() > tableHeaderLineNumber) {
                    keywords.add(keyword);
                }
            }

            Collections.reverse(keywords);
        }

        return keywords;
    }
}
