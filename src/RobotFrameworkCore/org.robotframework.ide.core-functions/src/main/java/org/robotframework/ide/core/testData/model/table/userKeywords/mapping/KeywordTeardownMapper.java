/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTeardown;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class KeywordTeardownMapper extends AKeywordSettingDeclarationMapper {

    public KeywordTeardownMapper() {
        super(RobotTokenType.KEYWORD_SETTING_TEARDOWN);
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_TEARDOWN);
        types.add(RobotTokenType.KEYWORD_THE_FIRST_ELEMENT);

        rt.setText(new StringBuilder(text));

        UserKeyword keyword = finder.findOrCreateNearestKeyword(currentLine,
                processingState, robotFileOutput, rt, fp);
        KeywordTeardown teardown = new KeywordTeardown(rt);
        keyword.addTeardown(teardown);

        processingState.push(ParsingState.KEYWORD_SETTING_TEARDOWN);

        return rt;
    }
}
