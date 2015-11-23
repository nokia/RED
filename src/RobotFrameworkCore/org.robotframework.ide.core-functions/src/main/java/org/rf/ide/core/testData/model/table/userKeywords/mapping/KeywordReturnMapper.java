/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testData.model.FilePosition;
import org.rf.ide.core.testData.model.RobotFileOutput;
import org.rf.ide.core.testData.model.table.userKeywords.KeywordReturn;
import org.rf.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.rf.ide.core.testData.text.read.IRobotTokenType;
import org.rf.ide.core.testData.text.read.ParsingState;
import org.rf.ide.core.testData.text.read.RobotLine;
import org.rf.ide.core.testData.text.read.recognizer.RobotToken;
import org.rf.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordReturnMapper extends AKeywordSettingDeclarationMapper {

    public KeywordReturnMapper() {
        super(RobotTokenType.KEYWORD_SETTING_RETURN);
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.KEYWORD_SETTING_RETURN);

        rt.setText(text);
        rt.setRaw(text);

        final UserKeyword keyword = finder.findOrCreateNearestKeyword(currentLine,
                processingState, robotFileOutput, rt, fp);
        final KeywordReturn returned = new KeywordReturn(rt);
        keyword.addReturn(returned);

        processingState.push(ParsingState.KEYWORD_SETTING_RETURN);

        return rt;
    }
}
