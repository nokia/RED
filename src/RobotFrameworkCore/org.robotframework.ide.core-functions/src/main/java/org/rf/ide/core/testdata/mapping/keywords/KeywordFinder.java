/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordFinder {

    public UserKeyword findOrCreateNearestKeyword(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp) {
        final RobotFile fileModel = robotFileOutput.getFileModel();
        final KeywordTable keywordTable = fileModel.getKeywordTable();

        UserKeyword keyword;
        final List<UserKeyword> lastHeaderKeyword = filterByKeywordAfterLastHeader(keywordTable);
        if (lastHeaderKeyword.isEmpty()) {
            keyword = createArtificialKeyword(robotFileOutput, keywordTable);
            keywordTable.addKeyword(keyword);

            final RobotLine lineToModification = findRobotLineInModel(fileModel,
                    keyword, currentLine);
            lineToModification.addLineElementAt(0, keyword.getKeywordName());
        } else {
            keyword = lastHeaderKeyword.get(lastHeaderKeyword.size() - 1);
        }

        return keyword;
    }

    private RobotLine findRobotLineInModel(final RobotFile fileModel,
            final UserKeyword userKeyword, final RobotLine currentLine) {
        RobotLine foundLine = currentLine;
        if (foundLine.getLineNumber() != userKeyword.getBeginPosition()
                .getLine()) {
            final List<RobotLine> fileContent = fileModel.getFileContent();
            for (final RobotLine line : fileContent) {
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
            final RobotFileOutput robotFileOutput, final KeywordTable keywordTable) {
        UserKeyword keyword;
        final List<TableHeader<? extends ARobotSectionTable>> headers = keywordTable
                .getHeaders();
        final TableHeader<?> tableHeader = headers.get(headers.size() - 1);
        final RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader()
                .getLineNumber() + 1);
        artificialNameToken.setText("");
        artificialNameToken.setStartColumn(0);
        final RobotLine robotLine = robotFileOutput.getFileModel().getFileContent()
                .get(tableHeader.getTableHeader().getLineNumber() - 1);
        final IRobotLineElement endOfLine = robotLine.getEndOfLine();
        artificialNameToken.setStartOffset(endOfLine.getStartOffset()
                + endOfLine.getRaw().length());
        artificialNameToken.setType(RobotTokenType.KEYWORD_NAME);

        keyword = new UserKeyword(artificialNameToken);

        return keyword;
    }

    public List<UserKeyword> filterByKeywordAfterLastHeader(
            final KeywordTable keywordTable) {
        final List<UserKeyword> keywords = new ArrayList<>();

        final List<TableHeader<? extends ARobotSectionTable>> headers = keywordTable
                .getHeaders();
        if (!headers.isEmpty()) {
            final List<UserKeyword> keywordsAvail = keywordTable.getKeywords();
            final TableHeader<?> tableHeader = headers.get(headers.size() - 1);
            final int tableHeaderLineNumber = tableHeader.getTableHeader()
                    .getLineNumber();
            final int numberOfTestCases = keywordsAvail.size();
            for (int i = numberOfTestCases - 1; i >= 0; i--) {
                final UserKeyword keyword = keywordsAvail.get(i);
                if (keyword.getKeywordName().getLineNumber() > tableHeaderLineNumber) {
                    keywords.add(keyword);
                }
            }

            Collections.reverse(keywords);
        }

        return keywords;
    }
}
