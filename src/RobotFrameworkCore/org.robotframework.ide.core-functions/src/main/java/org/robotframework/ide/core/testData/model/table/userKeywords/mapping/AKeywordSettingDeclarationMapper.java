package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public abstract class AKeywordSettingDeclarationMapper implements
        IParsingMapper {

    private final IRobotTokenType mappedType;


    protected AKeywordSettingDeclarationMapper(final IRobotTokenType mappedType) {
        this.mappedType = mappedType;
    }


    @VisibleForTesting
    protected UserKeyword findOrCreateNearestKeyword(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp) {
        KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();

        UserKeyword keyword;
        List<UserKeyword> lastHeaderKeyword = filterByKeywordAfterLastHeader(keywordTable);
        if (lastHeaderKeyword.isEmpty()) {
            keyword = createArtificialKeyword(keywordTable);
            keywordTable.addKeyword(keyword);
        } else {
            keyword = lastHeaderKeyword.get(lastHeaderKeyword.size() - 1);
        }

        return keyword;
    }


    private UserKeyword createArtificialKeyword(KeywordTable keywordTable) {
        UserKeyword keyword;
        List<TableHeader> headers = keywordTable.getHeaders();
        TableHeader tableHeader = headers.get(headers.size() - 1);
        RobotToken artificialNameToken = new RobotToken();
        artificialNameToken.setLineNumber(tableHeader.getTableHeader()
                .getLineNumber() + 1);
        artificialNameToken.setRaw(new StringBuilder());
        artificialNameToken.setText(new StringBuilder());
        artificialNameToken.setStartColumn(0);
        artificialNameToken.setType(RobotTokenType.KEYWORD_NAME);

        keyword = new UserKeyword(artificialNameToken);
        return keyword;
    }


    @VisibleForTesting
    protected List<UserKeyword> filterByKeywordAfterLastHeader(
            final KeywordTable keywordTable) {
        List<UserKeyword> keywords = new LinkedList<>();

        List<TableHeader> headers = keywordTable.getHeaders();
        if (!headers.isEmpty()) {
            List<UserKeyword> keywordsAvail = keywordTable.getKeywords();
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


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;

        if (rt.getTypes().get(0) == mappedType) {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size == 1) {
                List<IRobotTokenType> types = lineElements.get(0).getTypes();
                result = (types.contains(SeparatorType.PIPE) || types
                        .contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE));
            } else {
                for (IRobotLineElement elem : lineElements) {
                    List<IRobotTokenType> types = elem.getTypes();
                    if (types.contains(SeparatorType.PIPE)
                            || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)) {
                        continue;
                    } else if (types.contains(RobotTokenType.KEYWORD_NAME)) {
                        result = true;
                        break;
                    } else {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

}
