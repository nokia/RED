/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordArgumentsValueMapper implements IParsingMapper {

    private final ParsingStateHelper utility;

    public KeywordArgumentsValueMapper() {
        this.utility = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_ARGUMENT);
        rt.setText(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel()
                .getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);
        final List<KeywordArguments> arguments = keyword.getArguments();
        final KeywordArguments keywordArgument = arguments.get(arguments.size() - 1);
        keywordArgument.addArgument(rt);

        processingState
                .push(ParsingState.KEYWORD_SETTING_ARGUMENTS_ARGUMENT_VALUE);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);
        result = (state == ParsingState.KEYWORD_SETTING_ARGUMENTS
                || state == ParsingState.KEYWORD_SETTING_ARGUMENTS_ARGUMENT_VALUE);

        return result;
    }

}
